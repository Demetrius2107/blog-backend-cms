package com.demetrius.blog.article.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.blog.article.domain.article.entity.Article;
import com.demetrius.blog.article.domain.article.repository.ArticleRepository;
import com.demetrius.blog.article.infrastructure.persistence.converter.ArticleConverter;
import com.demetrius.blog.article.infrastructure.persistence.mapper.ArticleMapper;
import com.demetrius.blog.article.infrastructure.persistence.po.ArticlePO;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryImpl implements ArticleRepository {

    private final ArticleMapper articleMapper;
    private final ArticleConverter articleConverter;

    public ArticleRepositoryImpl(ArticleMapper articleMapper, ArticleConverter articleConverter) {
        this.articleMapper = articleMapper;
        this.articleConverter = articleConverter;
    }

    @Override
    public Article findById(Long id) {
        ArticlePO po = articleMapper.selectById(id);
        return po != null ? articleConverter.toDomain(po) : null;
    }

    @Override
    public Page<Article> findPage(long current, long size, Long categoryId) {
        LambdaQueryWrapper<ArticlePO> wrapper = new LambdaQueryWrapper<ArticlePO>()
                .eq(categoryId != null, ArticlePO::getCategoryId, categoryId)
                .orderByDesc(ArticlePO::getIsTop)
                .orderByDesc(ArticlePO::getCreateTime);

        Page<ArticlePO> poPage = articleMapper.selectPage(new Page<>(current, size), wrapper);

        Page<Article> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        domainPage.setRecords(poPage.getRecords().stream().map(articleConverter::toDomain).toList());
        return domainPage;
    }

    @Override
    public void save(Article article) {
        ArticlePO po = articleConverter.toPO(article);
        if (po.getId() == null) {
            articleMapper.insert(po);
            article.setId(po.getId());
        } else {
            articleMapper.updateById(po);
        }
    }

    @Override
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }
}
