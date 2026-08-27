package com.demetrius.vellastra.article.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demetrius.vellastra.article.infrastructure.persistence.po.ArticlePO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>Title: ArticleMapper</p>
 * <p>Description: 文章 Mapper（MyBatis-Plus）</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-05-17
 * @updateTime 2026-07-05
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@Mapper
public interface ArticleMapper extends BaseMapper<ArticlePO> {

    /**
     * 浏览量 +1
     */
    @Update("UPDATE blog_article SET view_count = view_count + 1 WHERE id = #{id}")
    void updateViewCount(Long id);

    /**
     * 查询点赞记录数（status=1 表示已点赞）
     */
    @Select("SELECT COUNT(1) FROM t_article_like WHERE article_id = #{articleId} AND user_id = #{userId} AND status = 1")
    Integer checkLikeExists(Long articleId, Long userId);

    /**
     * 取消点赞（软删：status 0=取消赞 1=已点赞，保留点赞记录便于审计/恢复）
     */
    @Update("UPDATE t_article_like SET status = 0 WHERE article_id = #{articleId} AND user_id = #{userId}")
    void cancelLike(Long articleId, Long userId);

    /**
     * 新增点赞（唯一索引 uk_article_user 防重复，冲突时恢复为已点赞）
     */
    @Insert("INSERT INTO t_article_like(article_id, user_id, status, create_time) " +
            "VALUES(#{articleId}, #{userId}, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE status = 1")
    void insertLike(Long articleId, Long userId);

    /**
     * 查询文章当前点赞数（供 Redis 冷启动回源）
     */
    @Select("SELECT COUNT(1) FROM t_article_like WHERE article_id = #{articleId} AND status = 1")
    Long countLikes(Long articleId);

    /**
     * 查询某文章全部已点赞用户ID（Redis 冷启动回源用）
     */
    @Select("SELECT user_id FROM t_article_like WHERE article_id = #{articleId} AND status = 1")
    List<Long> findLikedUserIds(Long articleId);

    /**
     * 原子更新文章点赞数（like_count = like_count ± delta，避免读后写丢失更新）
     *
     * @param delta 1=点赞 +1，-1=取消 -1
     */
    @Update("UPDATE blog_article SET like_count = like_count + #{delta} WHERE id = #{articleId}")
    void updateLikeCount(Long articleId, int delta);

    /**
     * 更新分类文章数（delta=1 增1，delta=-1 减1）
     */
    @Update("UPDATE t_category SET article_count = article_count + #{delta} WHERE id = #{categoryId}")
    void updateCategoryArticleCount(Long categoryId, int delta);
}
