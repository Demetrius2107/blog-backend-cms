package com.demetrius.vellastra.comment.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.vellastra.comment.domain.comment.entity.Comment;
import com.demetrius.vellastra.comment.domain.comment.repository.CommentRepository;
import com.demetrius.vellastra.comment.domain.comment.valueobject.CommentStatus;
import com.demetrius.vellastra.comment.interfaces.dto.*;
import com.demetrius.vellastra.common.exception.BizException;
import com.demetrius.vellastra.common.exception.ErrorCode;
import com.demetrius.vellastra.common.response.PageResult;
import org.springframework.stereotype.Service;

/**
 * <p>Title: CommentApplicationService</p>
 * <p>Description: 评论应用服务，负责评论的增删改查、回复、审核等业务逻辑</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @version 1.1
 * @since 2026-07-18
 */
@Service
public class CommentApplicationService {

    private final CommentRepository commentRepository;

    public CommentApplicationService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * 分页查询评论列表
     *
     * @param current   页码
     * @param size      每页条数
     * @param articleId 文章ID（可选）
     * @param status    审核状态（可选）
     * @return 分页评论列表
     */
    public PageResult<CommentVO> list(long current, long size, Long articleId, Integer status) {
        Page<Comment> page = commentRepository.findPage(current, size, articleId, status);
        return PageResult.of(
                page.getRecords().stream().map(this::toVO).toList(),
                page.getTotal(), current, size
        );
    }

    /**
     * 创建评论
     *
     * @param request 创建评论请求
     * @param userId  用户ID
     * @return 评论ID
     */
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

    /**
     * 回复评论
     *
     * @param request 回复评论请求
     * @param userId  用户ID
     * @return 评论ID
     */
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
                .replyUserId(replyTo.getUserId())
                .status(CommentStatus.PENDING.getCode())
                .likeCount(0)
                .build();
        comment.initCreateTime();
        commentRepository.save(comment);
        return comment.getId();
    }

    /**
     * 删除评论（仅评论作者本人或管理员可操作）
     *
     * @param id     评论ID
     * @param userId 当前用户ID
     * @param roles  当前用户角色（逗号分隔，含 1=超级管理员）
     */
    public void delete(Long id, Long userId, String roles) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw ErrorCode.COMMENT_NOT_FOUND.toException();
        }
        // 越权校验：仅评论作者本人或管理员可删除
        boolean isAdmin = roles != null && java.util.Arrays.stream(roles.split(","))
                .map(String::trim).anyMatch("1"::equals);
        if (!isAdmin && (userId == null || !userId.equals(comment.getUserId()))) {
            throw ErrorCode.FORBIDDEN.toException();
        }
        commentRepository.delete(id);
    }

    /**
     * 审核评论（仅管理员可操作）
     *
     * @param id     评论ID
     * @param status 目标状态（1-通过 2-拒绝）
     * @param roles  当前用户角色（逗号分隔，含 1=超级管理员）
     */
    public void audit(Long id, Integer status, String roles) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            throw ErrorCode.COMMENT_NOT_FOUND.toException();
        }
        // 越权校验：仅管理员（角色ID=1）可审核
        boolean isAdmin = roles != null && java.util.Arrays.stream(roles.split(","))
                .map(String::trim).anyMatch("1"::equals);
        if (!isAdmin) {
            throw ErrorCode.FORBIDDEN.toException();
        }
        comment.setStatus(status);
        comment.updateTime();
        commentRepository.save(comment);
    }

    /**
     * 领域对象转视图对象
     *
     * @param c 评论领域对象
     * @return 评论视图对象
     */
    private CommentVO toVO(Comment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setArticleId(c.getArticleId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId() != null && c.getParentId() > 0 ? c.getParentId() : null);
        vo.setReplyUserId(c.getReplyUserId());
        vo.setContent(c.getContent());
        vo.setStatus(c.getStatus());
        vo.setLikeCount(c.getLikeCount());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }
}
