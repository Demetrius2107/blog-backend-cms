package com.demetrius.blog.article.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.blog.article.domain.article.entity.Article;
import com.demetrius.blog.article.domain.article.repository.ArticleRepository;
import com.demetrius.blog.article.interfaces.dto.*;
import com.demetrius.blog.common.exception.ErrorCode;
import com.demetrius.blog.common.response.PageResult;
import org.springframework.stereotype.Service;

@Service
public class ArticleApplicationService {

    private final ArticleRepository articleRepository;

    public ArticleApplicationService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Long createArticle(CreateArticleRequest request, Long userId) {
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .coverImage(request.getCoverImage())
                .categoryId(request.getCategoryId())
                .status(request.getStatus() != null ? request.getStatus() : 0)
                .tags(request.getTags())
                .authorId(userId)
                .viewCount(0L)
                .likeCount(0L)
                .build();
        article.initCreateTime();
        articleRepository.save(article);
        return article.getId();
    }

    public void updateArticle(Long id, UpdateArticleRequest request) {
        Article article = articleRepository.findById(id);
        if (article == null) {
            throw ErrorCode.ARTICLE_NOT_FOUND.toException();
        }
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }
        article.setTags(request.getTags());
        article.updateTime();
        articleRepository.save(article);
    }

    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id);
        if (article == null) {
            throw ErrorCode.ARTICLE_NOT_FOUND.toException();
        }
        if (article.isPublished()) {
            throw ErrorCode.ARTICLE_PUBLISHED.toException();
        }
        articleRepository.delete(id);
    }

    public ArticleVO getArticleById(Long id) {
        Article article = articleRepository.findById(id);
        if (article == null) {
            throw ErrorCode.ARTICLE_NOT_FOUND.toException();
        }
        return toVO(article);
    }

    public PageResult<ArticleVO> listArticles(long current, long size, Long categoryId) {
        Page<Article> page = articleRepository.findPage(current, size, categoryId);
        return PageResult.of(
                page.getRecords().stream().map(this::toVO).toList(),
                page.getTotal(), current, size
        );
    }

    private ArticleVO toVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategoryId(article.getCategoryId());
        vo.setStatus(article.getStatus());
        vo.setTags(article.getTags());
        vo.setAuthorId(article.getAuthorId());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());
        return vo;
    }
}
