package com.demetrius.vellastra.mail.application;

import com.demetrius.vellastra.mail.config.MailProperties;
import com.demetrius.vellastra.mail.infrastructure.mapper.SubscriberMapper;
import com.demetrius.vellastra.mail.infrastructure.po.SubscriberPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberServiceTest {

    @Mock
    private SubscriberMapper subscriberMapper;

    @Mock
    private MailSenderService mailSenderService;

    private SubscriberService subscriberService;

    @BeforeEach
    void setUp() {
        MailProperties props = new MailProperties();
        props.setBaseUrl("http://localhost:8080");
        subscriberService = new SubscriberService(subscriberMapper,
                new MailTemplateRenderer(), mailSenderService, props);
    }

    @Test
    @DisplayName("subscribe 合法邮箱应保存并发送确认邮件")
    void subscribe_validEmail_shouldSaveAndSend() {
        when(subscriberMapper.selectOne(any())).thenReturn(null);
        when(mailSenderService.sendHtml(anyString(), anyString(), anyString())).thenReturn(true);
        // mock insert 时回填主键 id（MyBatis-Plus 真实行为）
        doAnswer(inv -> {
            SubscriberPO po = inv.getArgument(0);
            po.setId(1L);
            return 1;
        }).when(subscriberMapper).insert(any(SubscriberPO.class));

        Long id = subscriberService.subscribe("user@example.com", "张三");

        assertNotNull(id);
        assertEquals(1L, id);
        verify(subscriberMapper).insert(any(SubscriberPO.class));
        verify(mailSenderService).sendHtml(eq("user@example.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("subscribe 非法邮箱应抛异常")
    void subscribe_invalidEmail_shouldThrow() {
        assertThrows(RuntimeException.class, () -> subscriberService.subscribe("not-an-email", "张三"));
        verify(subscriberMapper, never()).insert(any(SubscriberPO.class));
    }

    @Test
    @DisplayName("confirm 有效 token 应确认订阅")
    void confirm_validToken_shouldConfirm() {
        SubscriberPO po = new SubscriberPO();
        po.setId(1L);
        po.setEmail("user@example.com");
        po.setStatus("pending");
        when(subscriberMapper.selectOne(any())).thenReturn(po);

        boolean confirmed = subscriberService.confirm("token-abc");

        assertTrue(confirmed);
        assertEquals("confirmed", po.getStatus());
        assertNotNull(po.getConfirmedAt());
    }

    @Test
    @DisplayName("confirm 无效 token 应返回 false")
    void confirm_invalidToken_shouldReturnFalse() {
        when(subscriberMapper.selectOne(any())).thenReturn(null);
        assertFalse(subscriberService.confirm("invalid-token"));
    }

    @Test
    @DisplayName("unsubscribe 应标记为已退订")
    void unsubscribe_shouldMarkUnsubscribed() {
        SubscriberPO po = new SubscriberPO();
        po.setId(1L);
        po.setEmail("user@example.com");
        po.setStatus("confirmed");
        when(subscriberMapper.selectOne(any())).thenReturn(po);

        boolean result = subscriberService.unsubscribe("token-xyz");

        assertTrue(result);
        assertEquals("unsubscribed", po.getStatus());
        assertNotNull(po.getUnsubscribedAt());
    }
}
