package com.demetrius.blog.comment.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.blog.comment.domain.comment.entity.Comment;
import com.demetrius.blog.comment.domain.comment.repository.CommentRepository;
import com.demetrius.blog.comment.interfaces.dto.*;
import com.demetrius.blog.common.exception.BizException;
import com.demetrius.blog.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CommentApplicationService {

    private final CommentRepository commentRepository;

    public CommentApplicationService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public PageResult<CommentVO> list(long current, long size, Long articleId, Integer status) {
        Page<Comment> page = commentRepository.findPage(current, size, articleId, status);
        return PageResult.of(
                page.getRecords().stream().map(this::toVO).toList(),
                page.getTotal(), current, size
        );
    }

    public Long create(CreateCommentRequest request, Long userId) {
        Comment comment = Comment.builder()
                .articleId(request.getArticleId())
                .userId(userId)
                .content(request.getContent())
                .parentId(0L)
                .status(CommentStatus.PENDING.getCode())
                .likeCount(0)
                .build();
        comment.initCreateTime();
        commentRepository.save(comment);
        return comment.getId();
    }

    public Long reply(ReplyCommentRequest request, Long userId) {
        Comment parent = commentRepository.findById(request.getParentId());
        if (parent == null) {
            throw ErrorCode.COMMENT_NOT_FOUND.toException();
        }

        Comment replyTo = commentRepository.findById(request.getReplyToId());
        if (replyTo == null) {
            throw ErrorCode.COMMENT_NOT_FOUND.toException();
        }

        Comment comment = Comment.builder()
                .articleId(request.getArticleId())
                .userId(userId)
                .content(request.getContent())
                .parentId(request.getParentId())
                .replyToId(request.getReplyToId())
                .replyToUserId(replyTo.getUserId())
                .status(CommentStatus.PENDING.getCode())
                .likeCount(0)
                .build();
        comment.initCreateTime();
        commentRepository.save(comment);
        return comment.getId();
    }

    public void delete(Long id) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw ErrorCode.COMMENT_NOT_FOUND.toException();
        }
        commentRepository.delete(id);
    }

    public void audit(Long id, Integer status) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw ErrorCode.COMMENT_NOT_FOUND.toException();
        }
        comment.setStatus(status);
        comment.updateTime();
        commentRepository.save(comment);
    }

    private CommentVO toVO(Comment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setArticleId(c.getArticleId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId() != null && c.getParentId() > 0 ? c.getParentId() : null);
        vo.setReplyToId(c.getReplyToId());
        vo.setReplyToUserId(c.getReplyToUserId());
        vo.setContent(c.getContent());
        vo.setStatus(c.getStatus());
        vo.setLikeCount(c.getLikeCount());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }
}
