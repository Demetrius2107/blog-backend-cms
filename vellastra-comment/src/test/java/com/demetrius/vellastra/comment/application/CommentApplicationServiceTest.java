package com.demetrius.vellastra.comment.application;

import com.demetrius.vellastra.comment.domain.comment.entity.Comment;
import com.demetrius.vellastra.comment.domain.comment.repository.CommentRepository;
import com.demetrius.vellastra.comment.interfaces.dto.CreateCommentRequest;
import com.demetrius.vellastra.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CommentApplicationService}
 */
@ExtendWith(MockitoExtension.class)
class CommentApplicationServiceTest {

    @Mock
    private CommentRepository commentRepository;

    private CommentApplicationService commentApplicationService;

    @BeforeEach
    void setUp() {
        commentApplicationService = new CommentApplicationService(commentRepository);
    }

    @Test
    @DisplayName("create 应保存评论并返回ID")
    void create_shouldSave() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setArticleId(1L);
        request.setContent("好文章！");

        doAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(1L);
            return null;
        }).when(commentRepository).save(any());

        Long id = commentApplicationService.create(request, 1L);
        assertEquals(1L, id);
    }

    @Test
    @DisplayName("delete 不存在时抛出异常")
    void delete_notFound_shouldThrow() {
        when(commentRepository.findById(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> commentApplicationService.delete(99L, 1L, "1"));
    }

    @Test
    @DisplayName("reply 应保存回复评论")
    void reply_shouldSave() {
        Comment parent = Comment.builder().id(1L).articleId(1L).build();
        when(commentRepository.findById(1L)).thenReturn(parent);
        doAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(2L);
            return null;
        }).when(commentRepository).save(any());

        Long id = commentApplicationService.reply(
                createReplyRequest(1L, 1L, "回复内容"), 2L);

        assertEquals(2L, id);
    }

    @Test
    @DisplayName("reply 父评论不存在时抛出异常")
    void reply_parentNotFound_shouldThrow() {
        when(commentRepository.findById(99L)).thenReturn(null);
        assertThrows(BizException.class, () ->
                commentApplicationService.reply(createReplyRequest(99L, 1L, "回复"), 1L));
    }

    @Test
    @DisplayName("audit 应更新评论状态")
    void audit_shouldUpdateStatus() {
        Comment comment = Comment.builder().id(1L).status(0).build();
        when(commentRepository.findById(1L)).thenReturn(comment);

        commentApplicationService.audit(1L, 1, "1");

        assertEquals(1, comment.getStatus());
    }

    @Test
    @DisplayName("audit 评论不存在时抛出异常")
    void audit_notFound_shouldThrow() {
        when(commentRepository.findById(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> commentApplicationService.audit(99L, 1, "1"));
    }

    private com.demetrius.vellastra.comment.interfaces.dto.ReplyCommentRequest createReplyRequest(
            Long parentId, Long replyToId, String content) {
        com.demetrius.vellastra.comment.interfaces.dto.ReplyCommentRequest req =
                new com.demetrius.vellastra.comment.interfaces.dto.ReplyCommentRequest();
        req.setArticleId(1L);
        req.setParentId(parentId);
        req.setReplyToId(replyToId);
        req.setContent(content);
        return req;
    }
}