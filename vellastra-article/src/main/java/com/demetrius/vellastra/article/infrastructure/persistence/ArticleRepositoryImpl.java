package com.demetrius.vellastra.article.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.vellastra.article.domain.article.entity.Article;
import com.demetrius.vellastra.article.domain.article.repository.ArticleRepository;
import com.demetrius.vellastra.article.infrastructure.persistence.converter.ArticleConverter;
import com.demetrius.vellastra.article.infrastructure.persistence.mapper.ArticleMapper;
import com.demetrius.vellastra.article.infrastructure.persistence.mapper.ArticleTagRelMapper;
import com.demetrius.vellastra.article.infrastructure.persistence.po.ArticlePO;
import com.demetrius.vellastra.article.infrastructure.persistence.po.ArticleTagRelPO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Title: ArticleRepositoryImpl</p>
 * <p>Description: 文章仓储实现（MyBatis-Plus），含分类/标签联动逻辑</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-05-17
 * @updateTime 2026-07-29
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {

    private final ArticleMapper articleMapper;
    private final ArticleConverter articleConverter;
    private final ArticleTagRelMapper articleTagRelMapper;

    public ArticleRepositoryImpl(ArticleMapper articleMapper, ArticleConverter articleConverter,
                                 ArticleTagRelMapper articleTagRelMapper) {
        this.articleMapper = articleMapper;
        this.articleConverter = articleConverter;
        this.articleTagRelMapper = articleTagRelMapper;
    }

    @Override
    public Article findById(Long id) {
        ArticlePO po = articleMapper.selectById(id);
        return po != null ? articleConverter.toDomain(po) : null;
    }

    @Override
    public Page<Article> findPage(long current, long size, Long categoryId,
                                  String keyword, Long authorId) {
        LambdaQueryWrapper<ArticlePO> wrapper = new LambdaQueryWrapper<ArticlePO>()
                .eq(categoryId != null, ArticlePO::getCategoryId, categoryId)
                .eq(authorId != null, ArticlePO::getAuthorId, authorId)
                .like(StringUtils.hasText(keyword), ArticlePO::getTitle, keyword)
                .orderByDesc(ArticlePO::getIsTop)
                .orderByDesc(ArticlePO::getCreateTime);

        Page<ArticlePO> poPage = articleMapper.selectPage(new Page<>(current, size), wrapper);

        Page<Article> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        domainPage.setRecords(poPage.getRecords().stream().map(articleConverter::toDomain).toList());
        return domainPage;
    }

    @Override
    @Transactional
    public void save(Article article) {
        ArticlePO po = articleConverter.toPO(article);
        boolean isNew = po.getId() == null;

        if (isNew) {
            articleMapper.insert(po);
            article.setId(po.getId());
        } else {
            articleMapper.updateById(po);
        }

        // ===== 分类联动：更新分类文章数 =====
        if (article.getCategoryId() != null && article.getCategoryId() > 0) {
            articleMapper.updateCategoryArticleCount(article.getCategoryId(), 1);
        }

        // ===== 标签联动：写入 t_article_tag =====
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            // 先清除旧关联（编辑时需重置）
            if (!isNew) {
                articleTagRelMapper.delete(new LambdaQueryWrapper<ArticleTagRelPO>()
                        .eq(ArticleTagRelPO::getArticleId, article.getId()));
            }
            // 插入新关联
            List<Long> tagIds = parseTagIds(article.getTags());
            for (Long tagId : tagIds) {
                ArticleTagRelPO rel = new ArticleTagRelPO();
                rel.setArticleId(article.getId());
                rel.setTagId(tagId);
                articleTagRelMapper.insert(rel);
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ArticlePO po = articleMapper.selectById(id);
        if (po != null) {
            // 分类联动：文章数 -1
            if (po.getCategoryId() != null && po.getCategoryId() > 0) {
                articleMapper.updateCategoryArticleCount(po.getCategoryId(), -1);
            }
            // 标签联动：清除关联
            articleTagRelMapper.delete(new LambdaQueryWrapper<ArticleTagRelPO>()
                    .eq(ArticleTagRelPO::getArticleId, id));
        }
        articleMapper.deleteById(id);
    }

    /**
     * 解析标签ID字符串（逗号分隔）为列表
     */
    private List<Long> parseTagIds(String tags) {
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public void updateViewCount(Long id) {
        articleMapper.updateViewCount(id);
    }

    @Override
    public boolean toggleLike(Long articleId, Long userId) {
        Integer count = articleMapper.checkLikeExists(articleId, userId);
        if (count != null && count > 0) {
            articleMapper.cancelLike(articleId, userId);
            return false;
        } else {
            articleMapper.insertLike(articleId, userId);
            return true;
        }
    }

    @Override
    public void syncLikeStatus(Long articleId, Long userId, boolean liked) {
        if (liked) {
            articleMapper.insertLike(articleId, userId);
        } else {
            articleMapper.cancelLike(articleId, userId);
        }
        articleMapper.updateLikeCount(articleId, liked ? 1 : -1);
    }

    @Override
    public void updateLikeCount(Long articleId, int delta) {
        articleMapper.updateLikeCount(articleId, delta);
    }

    @Override
    public List<Long> findLikedUserIds(Long articleId) {
        return articleMapper.findLikedUserIds(articleId);
    }

    @Override
    public Long countLikes(Long articleId) {
        return articleMapper.countLikes(articleId);
    }

    @Override
    public List<Article> findLatest(int size) {
        LambdaQueryWrapper<ArticlePO> wrapper = new LambdaQueryWrapper<ArticlePO>()
                .orderByDesc(ArticlePO::getCreateTime)
                .last("LIMIT " + size);
        List<ArticlePO> poList = articleMapper.selectList(wrapper);
        return poList.stream().map(articleConverter::toDomain).toList();
    }

    @Override
    public List<Article> findAllByStatus(Integer status) {
        LambdaQueryWrapper<ArticlePO> wrapper = new LambdaQueryWrapper<ArticlePO>()
                .eq(status != null, ArticlePO::getStatus, status)
                .orderByDesc(ArticlePO::getIsTop)
                .orderByDesc(ArticlePO::getPublishTime);
        List<ArticlePO> poList = articleMapper.selectList(wrapper);
        return poList.stream().map(articleConverter::toDomain).toList();
    }
}
