package com.demetrius.vellastra.article.domain.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.vellastra.article.domain.article.entity.Article;

import java.util.List;
import java.util.Optional;

/**
 * <p>Title: ArticleRepository</p>
 * <p>Description: 文章仓储接口</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-05-17
 * @updateTime 2026-07-05
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
public interface ArticleRepository {

    Article findById(Long id);

    Page<Article> findPage(long current, long size, Long categoryId, String keyword, Long authorId);

    void save(Article article);

    void delete(Long id);

    void updateViewCount(Long id);

    /**
     * 点赞切换
     * @return true = 已点赞（新增），false = 取消点赞
     */
    boolean toggleLike(Long articleId, Long userId);

    /**
     * 落库点赞状态（供 ArticleLikeService 异步将 Redis 最终状态同步到 DB）
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @param liked     true=点赞，false=取消
     */
    void syncLikeStatus(Long articleId, Long userId, boolean liked);

    /**
     * 原子更新文章点赞数（like_count = like_count ± delta）
     *
     * @param delta 1=点赞 +1，-1=取消 -1
     */
    void updateLikeCount(Long articleId, int delta);

    /**
     * 查询某文章全部已点赞用户ID（Redis 冷启动回源用）
     */
    List<Long> findLikedUserIds(Long articleId);

    /**
     * 查询文章当前点赞数（Redis 冷启动回源用）
     */
    Long countLikes(Long articleId);

    List<Article> findLatest(int size);

    /**
     * 按状态查询全部文章（不分页，供 SSG 导出使用）
     *
     * @param status 文章状态码（0草稿/1待审核/2已发布/3下架）
     * @return 符合状态的全部文章列表
     */
    List<Article> findAllByStatus(Integer status);
}
