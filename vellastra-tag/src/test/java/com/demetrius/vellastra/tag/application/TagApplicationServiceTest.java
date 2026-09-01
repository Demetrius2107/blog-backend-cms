package com.demetrius.vellastra.tag.application;

import com.demetrius.vellastra.tag.infrastructure.persistence.mapper.ArticleTagMapper;
import com.demetrius.vellastra.tag.infrastructure.persistence.mapper.TagMapper;
import com.demetrius.vellastra.tag.infrastructure.persistence.po.TagPO;
import com.demetrius.vellastra.tag.interfaces.dto.TagVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagApplicationServiceTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ArticleTagMapper articleTagMapper;

    private TagApplicationService tagApplicationService;

    @BeforeEach
    void setUp() {
        tagApplicationService = new TagApplicationService(tagMapper, articleTagMapper);
    }

    @Test
    @DisplayName("listAll 应返回所有启用标签")
    void listAll_shouldReturnEnabledTags() {
        TagPO po = new TagPO();
        po.setId(1L);
        po.setName("Java");
        po.setArticleCount(5);
        when(tagMapper.selectList(any())).thenReturn(List.of(po));

        List<TagVO> list = tagApplicationService.listAll();

        assertEquals(1, list.size());
        assertEquals("Java", list.get(0).getName());
    }

    @Test
    @DisplayName("create 应插入并返回新标签ID")
    void create_shouldInsertAndReturnId() {
        when(tagMapper.selectCount(any())).thenReturn(0L);
        doAnswer(inv -> {
            TagPO po = inv.getArgument(0);
            po.setId(1L);
            return 1;
        }).when(tagMapper).insert(any(TagPO.class));

        Long id = tagApplicationService.create("Spring", "spring");

        assertEquals(1L, id);
        verify(tagMapper).insert(any(TagPO.class));
    }

    @Test
    @DisplayName("create 名称重复时抛出异常")
    void create_duplicateName_shouldThrow() {
        when(tagMapper.selectCount(any())).thenReturn(1L);
        assertThrows(RuntimeException.class, () -> tagApplicationService.create("Java", "java"));
        verify(tagMapper, never()).insert(any(TagPO.class));
    }

    @Test
    @DisplayName("getHotTags 应返回限制数量的标签")
    void getHotTags_shouldReturnLimited() {
        TagPO po = new TagPO();
        po.setId(1L);
        po.setName("Java");
        when(tagMapper.selectList(any())).thenReturn(List.of(po));

        List<TagVO> list = tagApplicationService.getHotTags(10);

        assertEquals(1, list.size());
        verify(tagMapper).selectList(any());
    }

    @Test
    @DisplayName("update 不存在时抛出异常")
    void update_notFound_shouldThrow() {
        when(tagMapper.selectById(99L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> tagApplicationService.update(99L, "x", "y"));
    }

    @Test
    @DisplayName("delete 有关联文章时抛出异常")
    void delete_hasArticles_shouldThrow() {
        when(tagMapper.selectById(1L)).thenReturn(new TagPO());
        when(articleTagMapper.selectCount(any())).thenReturn(2L);

        assertThrows(RuntimeException.class, () -> tagApplicationService.delete(1L));
        verify(tagMapper, never()).deleteById(1L);
    }
}