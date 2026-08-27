package com.demetrius.vellastra.auth.infrastructure.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.demetrius.vellastra.auth.infrastructure.persistence.mapper.MenuMapper;
import com.demetrius.vellastra.auth.infrastructure.persistence.mapper.RoleMenuMapper;
import com.demetrius.vellastra.auth.infrastructure.persistence.po.MenuPO;
import com.demetrius.vellastra.auth.infrastructure.persistence.po.RoleMenuPO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <p>Title: PermissionServiceImplTest</p>
 * <p>Description: 权限查询服务实现单元测试</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-07-19
 * @updateTime 2026-07-19
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @Mock
    private MenuMapper menuMapper;

    private PermissionServiceImpl permissionService;

    @BeforeAll
    static void initTableInfo() {
        // 纯 Mockito 环境需要手动初始化 MyBatis-Plus 表元数据，
        // 否则 LambdaQueryWrapper 无法解析实体 lambda 缓存
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), RoleMenuPO.class);
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), MenuPO.class);
    }

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(roleMenuMapper, menuMapper);
    }

    @Test
    @DisplayName("根据角色ID查询权限列表，返回关联菜单的权限标识")
    void getPermissionsByRoleIds_shouldReturnPerms() {
        // 模拟角色关联菜单
        RoleMenuPO rm1 = new RoleMenuPO();
        rm1.setRoleId(2L);
        rm1.setMenuId(10L);
        RoleMenuPO rm2 = new RoleMenuPO();
        rm2.setRoleId(2L);
        rm2.setMenuId(20L);
        when(roleMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rm1, rm2));

        // 模拟菜单权限标识
        MenuPO menu1 = new MenuPO();
        menu1.setId(10L);
        menu1.setPerms("article:create");
        MenuPO menu2 = new MenuPO();
        menu2.setId(20L);
        menu2.setPerms("article:edit");
        when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(menu1, menu2));

        List<String> perms = permissionService.getPermissionsByRoleIds(List.of(2L));

        assertEquals(2, perms.size());
        assertTrue(perms.contains("article:create"));
        assertTrue(perms.contains("article:edit"));
    }

    @Test
    @DisplayName("角色ID列表为空时返回空列表")
    void getPermissionsByRoleIds_emptyRoles_shouldReturnEmpty() {
        List<String> perms = permissionService.getPermissionsByRoleIds(List.of());
        assertTrue(perms.isEmpty());
    }

    @Test
    @DisplayName("角色ID列表为 null 时返回空列表")
    void getPermissionsByRoleIds_nullRoles_shouldReturnEmpty() {
        List<String> perms = permissionService.getPermissionsByRoleIds(null);
        assertTrue(perms.isEmpty());
    }

    @Test
    @DisplayName("角色没有关联菜单时返回空列表")
    void getPermissionsByRoleIds_noMenuAssigned_shouldReturnEmpty() {
        when(roleMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<String> perms = permissionService.getPermissionsByRoleIds(List.of(99L));

        assertTrue(perms.isEmpty());
        verify(roleMenuMapper).selectList(any(LambdaQueryWrapper.class));
        // 不应查询菜单表
        verify(menuMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("菜单权限标识去重")
    void getPermissionsByRoleIds_shouldDeduplicatePerms() {
        // 两个角色关联同一个菜单
        RoleMenuPO rm1 = new RoleMenuPO();
        rm1.setRoleId(2L);
        rm1.setMenuId(10L);
        RoleMenuPO rm2 = new RoleMenuPO();
        rm2.setRoleId(3L);
        rm2.setMenuId(10L);
        when(roleMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rm1, rm2));

        MenuPO menu = new MenuPO();
        menu.setId(10L);
        menu.setPerms("article:create");
        when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(menu));

        List<String> perms = permissionService.getPermissionsByRoleIds(List.of(2L, 3L));

        assertEquals(1, perms.size());
        assertEquals("article:create", perms.get(0));
    }

    @Test
    @DisplayName("权限标识为空的菜单不返回")
    void getPermissionsByRoleIds_shouldSkipEmptyPerms() {
        RoleMenuPO rm = new RoleMenuPO();
        rm.setRoleId(2L);
        rm.setMenuId(10L);
        when(roleMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rm));

        // 菜单权限标识为空
        MenuPO menu = new MenuPO();
        menu.setId(10L);
        menu.setPerms(null);
        when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(menu));

        List<String> perms = permissionService.getPermissionsByRoleIds(List.of(2L));

        assertTrue(perms.isEmpty());
    }
}