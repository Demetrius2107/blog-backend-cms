package com.demetrius.blog.comment.domain.comment.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.blog.comment.domain.comment.entity.Comment;

public interface CommentRepository {

    Comment findById(Long id);

    Page<Comment> findPage(long current, long size, Long articleId, Integer status);

    void save(Comment comment);

    void delete(Long id);
}
