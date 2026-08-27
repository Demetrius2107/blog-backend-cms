package com.demetrius.vellastra.article.domain.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.vellastra.article.domain.article.entity.Article;

import java.util.List;

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

    List<Article> findLatest(int size);

    /**
     * 按状态查询全部文章（不分页，供 SSG 导出使用）
     *
     * @param status 文章状态码（0草稿/1待审核/2已发布/3下架）
     * @return 符合状态的全部文章列表
     */
    List<Article> findAllByStatus(Integer status);
}
