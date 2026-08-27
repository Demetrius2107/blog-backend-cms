package com.demetrius.vellastra.article.application;

import com.demetrius.vellastra.article.application.event.ArticleLikeEvent;
import com.demetrius.vellastra.article.domain.article.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Title: ArticleLikeService</p>
 * <p>Description: 文章点赞服务——Redis Set 热数据 + 异步落库最终一致</p>
 *
 * <p>设计动机（相对旧实现的三处改进）：</p>
 * <ol>
 *   <li><b>并发竞态</b>：旧实现「先查 DB 再写」是 read-check-write，两个并发请求都会读到
 *       "未点赞" 然后都插入，撞唯一索引报错。Redis Set 的 SADD/SREM 是原子的，天然防重。</li>
 *   <li><b>高频写放大</b>：点赞每次 toggle 都写 DB。改为 Redis 响应读请求，
 *       通过事件异步落库，DB 写频次大幅下降。</li>
 *   <li><b>丢失更新</b>：旧实现在应用层读 likeCount 再 +1 写回（读后写），并发下丢失更新。
 *       现在 DB 侧用 UPDATE ... SET like_count = like_count ± 1 原子增减。</li>
 * </ol>
 *
 * <p>容错：Redis 不可用时降级为直接 DB 操作（走 {@link ArticleRepository#syncLikeStatus}），
 * 保证功能可用性优先于性能。</p>
 *
 * @author wanqiu
 * @since 2.0
 */
@Slf4j
@Service
public class ArticleLikeService {

    /** Redis key 前缀：article:like:{articleId} → Set<userId> */
    private static final String LIKE_KEY_PREFIX = "article:like:";

    private final StringRedisTemplate redisTemplate;
    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ArticleLikeService(StringRedisTemplate redisTemplate,
                              ArticleRepository articleRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.redisTemplate = redisTemplate;
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 切换点赞状态。
     *
     * @return true=本次操作后为已点赞，false=本次操作后为取消点赞
     */
    public boolean toggleLike(Long articleId, Long userId) {
        String key = likeKey(articleId);
        String member = String.valueOf(userId);
        try {
            ensureLoaded(key, articleId);
            // SADD 返回新增成员数：1 表示之前不在集合中（未点赞→点赞），0 表示已存在
            Long added = redisTemplate.opsForSet().add(key, member);
            if (added != null && added > 0) {
                publishSyncEvent(articleId, userId, true);
                return true;
            }
            // 已在集合中 → 取消点赞
            redisTemplate.opsForSet().remove(key, member);
            publishSyncEvent(articleId, userId, false);
            return false;
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis 不可用，点赞降级为直接写 DB: articleId={}, userId={}", articleId, userId);
            return toggleInDb(articleId, userId);
        }
    }

    /**
     * 当前用户是否已点赞
     */
    public boolean isLiked(Long articleId, Long userId) {
        String key = likeKey(articleId);
        try {
            ensureLoaded(key, articleId);
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, String.valueOf(userId)));
        } catch (RedisConnectionFailureException e) {
            return articleRepository.findLikedUserIds(articleId).stream()
                    .anyMatch(id -> id.equals(userId));
        }
    }

    /**
     * 获取点赞数（优先 Redis SCARD，冷启动回源 DB）
     */
    public long getLikeCount(Long articleId) {
        String key = likeKey(articleId);
        try {
            ensureLoaded(key, articleId);
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0L;
        } catch (RedisConnectionFailureException e) {
            Long count = articleRepository.countLikes(articleId);
            return count != null ? count : 0L;
        }
    }

    /**
     * Redis key 不存在时从 DB 回源（冷启动 / Redis 重启后）。
     * 用分布式锁防并发回源放大，锁用 SET NX EX 实现，避免多个实例同时回源。
     */
    private void ensureLoaded(String key, Long articleId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        // 简单防击穿：用 SETNX 作为回源互斥锁，防止并发请求同时打 DB
        String lockKey = key + ":lock";
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1",
                java.time.Duration.ofSeconds(10));
        try {
            if (Boolean.TRUE.equals(locked)) {
                List<Long> likedUserIds = articleRepository.findLikedUserIds(articleId);
                if (!likedUserIds.isEmpty()) {
                    String[] members = likedUserIds.stream().map(String::valueOf).toArray(String[]::new);
                    redisTemplate.opsForSet().add(key, members);
                } else {
                    // 空集合也占位，防止每次请求都回源（防穿透）
                    redisTemplate.opsForSet().add(key, "__empty__");
                }
            }
        } finally {
            if (Boolean.TRUE.equals(locked)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    /**
     * 发布异步落库事件
     */
    private void publishSyncEvent(Long articleId, Long userId, boolean liked) {
        try {
            eventPublisher.publishEvent(new ArticleLikeEvent(articleId, userId, liked));
        } catch (Exception e) {
            // 事件发布失败不应阻断主流程，降级直接同步落库
            log.warn("点赞事件发布失败，降级直接落库: articleId={}, userId={}, liked={}",
                    articleId, userId, liked, e);
            articleRepository.syncLikeStatus(articleId, userId, liked);
        }
    }

    /**
     * Redis 不可用时降级：直接查 DB 判断当前状态再切换
     */
    private boolean toggleInDb(Long articleId, Long userId) {
        boolean currentlyLiked = articleRepository.findLikedUserIds(articleId).stream()
                .anyMatch(id -> id.equals(userId));
        boolean newLiked = !currentlyLiked;
        articleRepository.syncLikeStatus(articleId, userId, newLiked);
        return newLiked;
    }

    private String likeKey(Long articleId) {
        return LIKE_KEY_PREFIX + articleId;
    }
}
