/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.manager.config.PermissionProperties;
import com.openjiuwen.studio.agent.manager.entity.RolePermission;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.PermissionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DynamicPermissionRefreshTaskTest {

    Map<String, List<String>> roleAndPermission = new HashMap<>();

    @Mock
    private PermissionProperties permissionProperties;

    @Mock
    private PermissionService permissionService;

    @Mock
    private MgObsService obsService;

    @InjectMocks
    private DynamicPermissionRefreshTask refreshTask;

    private String jsonString;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // 构造RolePermission对象列表
        RolePermission owner = new RolePermission();
        owner.setRoleName("OWNER");
        owner.setRoleDescription("OWNER：空间所有者");
        owner.setPermissions(Arrays.asList("updateDependency", "queryWorkspaceMemberDetail"));

        RolePermission admin = new RolePermission();
        admin.setRoleName("ADMIN");
        admin.setRoleDescription("ADMIN：空间管理员");
        admin.setPermissions(Arrays.asList("updateDependency", "queryWorkspaceMemberDetail"));
        List<RolePermission> rolePermission = Arrays.asList(owner, admin);
        for (RolePermission rp : rolePermission) {
            roleAndPermission.put(rp.getRoleName(), rp.getPermissions());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        jsonString = objectMapper.writeValueAsString(rolePermission);
    }

    @Test
    void testRefreshPermissions_Success() {
        // Given
        String obsUrl = "http://obs.example.com/permissions.json";
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn(obsUrl);
        when(obsService.downloadObsFile(obsUrl)).thenReturn(jsonString);
        when(permissionService.updateDynamicPermissions(roleAndPermission)).thenReturn(true);

        // When
        boolean result = refreshTask.refreshPermissions();

        // Then
        assertTrue(result);
        verify(obsService).downloadObsFile(obsUrl);
        verify(permissionService).updateDynamicPermissions(roleAndPermission);
    }

    @Test
    void testRefreshPermissions_DownloadFailed() {
        // Given
        String obsUrl = "http://obs.example.com/permissions.json";
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn(obsUrl);
        when(obsService.downloadObsFile(obsUrl)).thenReturn(null);

        // When
        boolean result = refreshTask.refreshPermissions();

        // Then
        assertFalse(result);
        verify(obsService).downloadObsFile(obsUrl);
        verify(permissionService, never()).updateDynamicPermissions(any());
    }

    @Test
    void testRefreshPermissions_EmptyPermissions() {
        // Given
        String obsUrl = "http://obs.example.com/permissions.json";
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn(obsUrl);
        when(obsService.downloadObsFile(obsUrl)).thenReturn("");

        // When
        boolean result = refreshTask.refreshPermissions();

        // Then
        assertFalse(result);
        verify(obsService).downloadObsFile(obsUrl);
        verify(permissionService, never()).updateDynamicPermissions(any());
    }

    @Test
    void testRefreshPermissions_UpdateFailed() {
        // Given
        String obsUrl = "http://obs.example.com/permissions.json";
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn(obsUrl);
        when(obsService.downloadObsFile(obsUrl)).thenReturn(jsonString);
        when(permissionService.updateDynamicPermissions(roleAndPermission)).thenReturn(false);

        // When
        boolean result = refreshTask.refreshPermissions();

        // Then
        assertFalse(result);
        verify(obsService).downloadObsFile(obsUrl);
        verify(permissionService).updateDynamicPermissions(roleAndPermission);
    }

    @Test
    void testRefreshPermissions_NoObsUrl() {
        // Given
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn(null);

        // When
        boolean result = refreshTask.refreshPermissions();

        // Then
        assertFalse(result);
        verify(obsService, never()).downloadObsFile(anyString());
        verify(permissionService, never()).updateDynamicPermissions(any());
    }

    @Test
    void testRefreshPermissions_EmptyObsUrl() {
        // Given
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn("");

        // When
        boolean result = refreshTask.refreshPermissions();

        // Then
        assertFalse(result);
        verify(permissionService, never()).updateDynamicPermissions(any());
    }

    @Test
    void testScheduledRefresh_NegativeInterval() {
        // Given
        when(permissionProperties.getRefreshIntervalMs()).thenReturn(-1L);

        // When - 直接调用定时任务方法
        refreshTask.scheduledRefresh();

        // Then - 验证没有执行刷新逻辑
        verify(obsService, never()).downloadObsFile(anyString());
        verify(permissionService, never()).updateDynamicPermissions(any());
    }

    @Test
    void testScheduledRefresh_NoObsUrl() {
        // Given
        when(permissionProperties.getRefreshIntervalMs()).thenReturn(600000L);
        when(permissionProperties.getObsRolePermissionUrl()).thenReturn(null);

        // When
        refreshTask.scheduledRefresh();

        // Then
        verify(obsService, never()).downloadObsFile(anyString());
        verify(permissionService, never()).updateDynamicPermissions(any());
    }

    @Test
    void testGetTaskStatus() {
        // Given
        when(permissionService.hasDynamicPermissions()).thenReturn(true);

        // When
        String status = refreshTask.getTaskStatus();

        // Then
        assertThat(status).contains("hasDynamic: true");
    }
}
