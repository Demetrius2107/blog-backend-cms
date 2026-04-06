package com.demetrius.blog.article.infrastructure.persistence.converter;

import com.demetrius.blog.article.domain.article.entity.Article;
import com.demetrius.blog.article.infrastructure.persistence.po.ArticlePO;
import org.springframework.stereotype.Component;

@Component
public class ArticleConverter {

    public Article toDomain(ArticlePO po) {
        if (po == null) return null;
        return Article.builder()
                .id(po.getId())
                .title(po.getTitle())
                .summary(po.getSummary())
                .content(po.getContent())
                .coverImage(po.getCoverImage())
                .categoryId(po.getCategoryId())
                .status(po.getStatus())
                .setIsTop(po.getIsTop())
                .tags(po.getTags())
                .authorId(po.getAuthorId())
                .viewCount(po.getViewCount())
                .likeCount(po.getLikeCount())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    public ArticlePO toPO(Article domain) {
        if (domain == null) return null;
        ArticlePO po = new ArticlePO();
        po.setId(domain.getId());
        po.setTitle(domain.getTitle());
        po.setSummary(domain.getSummary());
        po.setContent(domain.getContent());
        po.setCoverImage(domain.getCoverImage());
        po.setCategoryId(domain.getCategoryId());
        po.setStatus(domain.getStatus());
        po.setIsTop(domain.getIsTop());
        po.setTags(domain.getTags());
        po.setAuthorId(domain.getAuthorId());
        po.setViewCount(domain.getViewCount());
        po.setLikeCount(domain.getLikeCount());
        po.setCommentCount(domain.getCommentCount());
        po.setPublishTime(domain.getPublishTime());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }
}
