package com.demetrius.vellastra.article.interfaces.facade;

import com.demetrius.vellastra.article.application.ArticleApplicationService;
import com.demetrius.vellastra.article.domain.article.entity.Article;
import com.demetrius.vellastra.article.domain.article.valueobject.ArticleStatus;
import com.demetrius.vellastra.article.interfaces.dto.*;
import com.demetrius.vellastra.common.exception.BizException;
import com.demetrius.vellastra.common.response.PageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文章控制器功能测试（MockMvc + Mock Service）
 *
 * 使用 @SpringBootTest + @AutoConfigureMockMvc 加载完整上下文（含 MyBatis），
 * 通过 @MockitoBean 模拟 ArticleApplicationService，
 * 验证 HTTP 请求/响应、参数绑定、异常处理等。
 */
@DisplayName("ArticleController 控制器")
@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArticleApplicationService articleApplicationService;

    private ArticleVO sampleArticleVO;
    private CreateArticleRequest createRequest;
    private UpdateArticleRequest updateRequest;
    private BatchArticleRequest batchRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        sampleArticleVO = new ArticleVO();
        sampleArticleVO.setId(1L);
        sampleArticleVO.setTitle("测试文章");
        sampleArticleVO.setContent("测试内容");
        sampleArticleVO.setSummary("测试摘要");
        sampleArticleVO.setCoverImage("https://example.com/cover.jpg");
        sampleArticleVO.setCategoryId(1L);
        sampleArticleVO.setStatus(ArticleStatus.PUBLISHED.getCode());
        sampleArticleVO.setAuthorId(100L);
        sampleArticleVO.setViewCount(100L);
        sampleArticleVO.setLikeCount(10L);
        sampleArticleVO.setIsTop(1);
        sampleArticleVO.setCommentCount(5);
        sampleArticleVO.setPublishTime(now);
        sampleArticleVO.setCreateTime(now);
        sampleArticleVO.setUpdateTime(now);

        createRequest = new CreateArticleRequest();
        createRequest.setTitle("新文章标题");
        createRequest.setContent("新文章内容");
        createRequest.setSummary("新文章摘要");

        updateRequest = new UpdateArticleRequest();
        updateRequest.setTitle("更新后的标题");
        updateRequest.setContent("更新后的内容");

        batchRequest = new BatchArticleRequest();
        batchRequest.setIds(List.of(1L, 2L));
        batchRequest.setAction("delete");
    }

    // ======================== POST /article ========================

    @Nested
    @DisplayName("POST /article - 创建文章")
    class CreateArticle {

        @Test
        @DisplayName("应返回 200 和文章 ID")
        void shouldReturnCreatedArticleId() throws Exception {
            when(articleApplicationService.createArticle(any(CreateArticleRequest.class), eq(100L)))
                    .thenReturn(1L);

            mockMvc.perform(post("/article")
                            .header("X-User-Id", 100L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1));
        }

        @Test
        @DisplayName("缺少 X-User-Id 请求头应返回 400")
        void shouldReturn400WhenMissingUserIdHeader() throws Exception {
            mockMvc.perform(post("/article")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("空标题应返回 400")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            createRequest.setTitle("");

            mockMvc.perform(post("/article")
                            .header("X-User-Id", 100L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("空内容应返回 400")
        void shouldReturn400WhenContentIsBlank() throws Exception {
            createRequest.setContent("");

            mockMvc.perform(post("/article")
                            .header("X-User-Id", 100L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ======================== PUT /article/{id} ========================

    @Nested
    @DisplayName("PUT /article/{id} - 更新文章")
    class UpdateArticle {

        @Test
        @DisplayName("应返回 200")
        void shouldReturn200() throws Exception {
            doNothing().when(articleApplicationService).updateArticle(eq(1L), any(UpdateArticleRequest.class), any(), any());

            mockMvc.perform(put("/article/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("空标题应返回 400")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            updateRequest.setTitle("");

            mockMvc.perform(put("/article/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("文章不存在应返回 404")
        void shouldReturn404WhenArticleNotFound() throws Exception {
            doThrow(new BizException(3001, "文章不存在"))
                    .when(articleApplicationService).updateArticle(eq(999L), any(UpdateArticleRequest.class), any(), any());

            mockMvc.perform(put("/article/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(3001))
                    .andExpect(jsonPath("$.message").value("文章不存在"));
        }
    }

    // ======================== DELETE /article/{id} ========================

    @Nested
    @DisplayName("DELETE /article/{id} - 删除文章")
    class DeleteArticle {

        @Test
        @DisplayName("应返回 200")
        void shouldReturn200() throws Exception {
            doNothing().when(articleApplicationService).deleteArticle(1L, null, null);

            mockMvc.perform(delete("/article/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除已发布文章应返回业务错误")
        void shouldReturnBizErrorWhenPublished() throws Exception {
            doThrow(new BizException(3002, "文章已发布，无法删除"))
                    .when(articleApplicationService).deleteArticle(2L, null, null);

            mockMvc.perform(delete("/article/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(3002))
                    .andExpect(jsonPath("$.message").value("文章已发布，无法删除"));
        }
    }

    // ======================== GET /article/{id} ========================

    @Nested
    @DisplayName("GET /article/{id} - 查看文章详情")
    class GetArticle {

        @Test
        @DisplayName("应返回文章详情")
        void shouldReturnArticle() throws Exception {
            when(articleApplicationService.getArticleById(1L)).thenReturn(sampleArticleVO);

            mockMvc.perform(get("/article/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("测试文章"))
                    .andExpect(jsonPath("$.data.content").value("测试内容"))
                    .andExpect(jsonPath("$.data.status").value(ArticleStatus.PUBLISHED.getCode()));
        }

        @Test
        @DisplayName("文章不存在应返回业务错误")
        void shouldReturn404WhenNotFound() throws Exception {
            when(articleApplicationService.getArticleById(999L))
                    .thenThrow(new BizException(3001, "文章不存在"));

            mockMvc.perform(get("/article/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(3001))
                    .andExpect(jsonPath("$.message").value("文章不存在"));
        }
    }

    // ======================== GET /article ========================

    @Nested
    @DisplayName("GET /article - 分页查询文章列表")
    class ListArticles {

        @Test
        @DisplayName("应返回分页文章列表")
        void shouldReturnPagedArticles() throws Exception {
            PageResult<ArticleVO> pageResult = PageResult.of(
                    List.of(sampleArticleVO), 1, 1, 10);
            when(articleApplicationService.listArticles(1, 10, null, null, null))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/article")
                            .param("current", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(1))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("应支持分类和关键词筛选参数")
        void shouldSupportFilterParams() throws Exception {
            PageResult<ArticleVO> emptyPage = PageResult.of(List.of(), 0, 1, 10);
            when(articleApplicationService.listArticles(1, 10, 1L, "关键词", 100L))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/article")
                            .param("current", "1")
                            .param("size", "10")
                            .param("categoryId", "1")
                            .param("keyword", "关键词")
                            .param("authorId", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("不带参数时应使用默认值 current=1, size=10")
        void shouldUseDefaultPagination() throws Exception {
            PageResult<ArticleVO> emptyPage = PageResult.of(List.of(), 0, 1, 10);
            when(articleApplicationService.listArticles(1, 10, null, null, null))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/article"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(articleApplicationService).listArticles(1, 10, null, null, null);
        }
    }

    // ======================== PATCH /article/{id}/publish ========================

    @Nested
    @DisplayName("PATCH /article/{id}/publish - 发布文章")
    class PublishArticle {

        @Test
        @DisplayName("应返回 200")
        void shouldReturn200() throws Exception {
            doNothing().when(articleApplicationService).publish(1L);

            mockMvc.perform(patch("/article/1/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ======================== PATCH /article/{id}/withdraw ========================

    @Nested
    @DisplayName("PATCH /article/{id}/withdraw - 撤回文章")
    class WithdrawArticle {

        @Test
        @DisplayName("应返回 200")
        void shouldReturn200() throws Exception {
            doNothing().when(articleApplicationService).withdraw(1L);

            mockMvc.perform(patch("/article/1/withdraw"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ======================== PATCH /article/{id}/top ========================

    @Nested
    @DisplayName("PATCH /article/{id}/top - 设置/取消置顶")
    class TopArticle {

        @Test
        @DisplayName("置顶应返回 200")
        void shouldReturn200WhenSetTop() throws Exception {
            doNothing().when(articleApplicationService).topArticle(1L, true);

            mockMvc.perform(patch("/article/1/top")
                            .param("top", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("取消置顶应返回 200")
        void shouldReturn200WhenUnsetTop() throws Exception {
            doNothing().when(articleApplicationService).topArticle(1L, false);

            mockMvc.perform(patch("/article/1/top")
                            .param("top", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ======================== POST /article/{id}/view ========================

    @Nested
    @DisplayName("POST /article/{id}/view - 浏览计数")
    class ViewArticle {

        @Test
        @DisplayName("应返回 200")
        void shouldReturn200() throws Exception {
            doNothing().when(articleApplicationService).incrementViewCount(1L, "127.0.0.1");

            mockMvc.perform(post("/article/1/view"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ======================== POST /article/{id}/like ========================

    @Nested
    @DisplayName("POST /article/{id}/like - 点赞/取消点赞")
    class LikeArticle {

        @Test
        @DisplayName("应返回 200")
        void shouldReturn200() throws Exception {
            when(articleApplicationService.toggleLike(1L, 100L)).thenReturn(true);

            mockMvc.perform(post("/article/1/like")
                            .header("X-User-Id", 100L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("缺少 X-User-Id 应返回 400")
        void shouldReturn400WhenMissingUserId() throws Exception {
            mockMvc.perform(post("/article/1/like"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ======================== GET /article/latest ========================

    @Nested
    @DisplayName("GET /article/latest - 最新文章")
    class LatestArticles {

        @Test
        @DisplayName("应返回最新文章列表")
        void shouldReturnLatestArticles() throws Exception {
            when(articleApplicationService.getLatestArticles(5))
                    .thenReturn(List.of(sampleArticleVO));

            mockMvc.perform(get("/article/latest")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("不带 size 参数时应使用默认值 5")
        void shouldUseDefaultSize() throws Exception {
            when(articleApplicationService.getLatestArticles(5))
                    .thenReturn(List.of());

            mockMvc.perform(get("/article/latest"))
                    .andExpect(status().isOk());

            verify(articleApplicationService).getLatestArticles(5);
        }
    }

    // ======================== POST /article/batch ========================

    @Nested
    @DisplayName("POST /article/batch - 批量操作")
    class BatchOperation {

        @Test
        @DisplayName("批量删除应返回 200")
        void shouldReturn200() throws Exception {
            doNothing().when(articleApplicationService).batchOperation(any(BatchArticleRequest.class));

            mockMvc.perform(post("/article/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(batchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("空 ID 列表应返回 400")
        void shouldReturn400WhenIdsEmpty() throws Exception {
            batchRequest.setIds(List.of());

            mockMvc.perform(post("/article/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(batchRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("空 action 应返回 400")
        void shouldReturn400WhenActionEmpty() throws Exception {
            batchRequest.setAction("");

            mockMvc.perform(post("/article/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(batchRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}