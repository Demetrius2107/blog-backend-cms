package com.demetrius.blog.article.domain.article.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.blog.article.domain.article.entity.Article;

public interface ArticleRepository {

    Article findById(Long id);

    Page<Article> findPage(long current, long size, Long categoryId);

    void save(Article article);

    void delete(Long id);
}
