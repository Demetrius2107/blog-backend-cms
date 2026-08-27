package com.demetrius.vellastra.article.application.event;

import com.demetrius.vellastra.article.domain.article.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * <p>Title: ArticleLikeEventListener</p>
 * <p>Description: 点赞事件异步监听器——将 Redis 最终状态同步落库</p>
 *
 * <p>落库内容：</p>
 * <ol>
 *   <li>t_article_like 记录（软删：status 1=点赞 0=取消，ON DUPLICATE KEY UPDATE 幂等）</li>
 *   <li>blog_article.like_count 原子增减（UPDATE ... SET like_count = like_count ± 1）</li>
 * </ol>
 *
 * <p>最终一致性：Redis 保证读时一致，DB 通过异步对账保证最终一致。
 * 监听器失败不影响主流程（点赞已记入 Redis），仅记录告警日志，
 * 可后续通过定时对账任务补偿。</p>
 *
 * @author wanqiu
 * @since 2.0
 */
@Slf4j
@Component
public class ArticleLikeEventListener {

    private final ArticleRepository articleRepository;

    public ArticleLikeEventListener(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Async
    @EventListener
    public void onLikeChanged(ArticleLikeEvent event) {
        try {
            articleRepository.syncLikeStatus(event.getArticleId(), event.getUserId(), event.isLiked());
            log.debug("点赞落库完成: articleId={}, userId={}, liked={}",
                    event.getArticleId(), event.getUserId(), event.isLiked());
        } catch (Exception e) {
            // 落库失败不回滚 Redis（Redis 是真相源），记录告警待对账补偿
            log.error("点赞落库失败（待对账补偿）: articleId={}, userId={}, liked={}, error={}",
                    event.getArticleId(), event.getUserId(), event.isLiked(), e.getMessage(), e);
        }
    }
}
