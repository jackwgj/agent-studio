/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @InjectMocks
    private PermissionService permissionService;

    private Map<String, List<String>> presetPermissions;

    private Map<String, List<String>> dynamicPermissions;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        presetPermissions = new HashMap<>();
        presetPermissions.put("admin", Arrays.asList("user:create", "user:delete"));
        presetPermissions.put("user", Arrays.asList("user:read"));

        dynamicPermissions = new HashMap<>();
        dynamicPermissions.put("admin", Arrays.asList("user:create", "user:delete", "user:update"));
        dynamicPermissions.put("operator", Arrays.asList("user:read", "user:update"));

        permissionService.setPresetPermissions(presetPermissions);
    }

    @Test
    void testSetPresetPermissions() {
        // When
        permissionService.setPresetPermissions(presetPermissions);

        // Then
        Map<String, List<String>> result = permissionService.getPresetPermissions();
        assertThat(result).hasSize(2);
        assertThat(result.get("admin")).containsExactly("user:create", "user:delete");
    }

    @Test
    void testUpdateDynamicPermissions_Success() {
        // When
        boolean result = permissionService.updateDynamicPermissions(dynamicPermissions);

        // Then
        assertTrue(result);
        assertTrue(permissionService.hasDynamicPermissions());
        Map<String, List<String>> currentDynamic = permissionService.getCurrentDynamicPermissions();
        assertThat(currentDynamic.get("admin")).containsExactly("user:create", "user:delete", "user:update");
    }

    @Test
    void testUpdateDynamicPermissions_InvalidRoleName() {
        // Given
        Map<String, List<String>> invalidPermissions = new HashMap<>();
        invalidPermissions.put("", Arrays.asList("user:read")); // 空角色名

        // When
        boolean result = permissionService.updateDynamicPermissions(invalidPermissions);

        // Then
        assertFalse(result);
        assertFalse(permissionService.hasDynamicPermissions());
    }

    @Test
    void testUpdateDynamicPermissions_InvalidPermission() {
        // Given
        Map<String, List<String>> invalidPermissions = new HashMap<>();
        invalidPermissions.put("admin", Arrays.asList("")); // 空权限

        // When
        boolean result = permissionService.updateDynamicPermissions(invalidPermissions);

        // Then
        assertFalse(result);
        assertFalse(permissionService.hasDynamicPermissions());
    }

    @Test
    void testUpdateDynamicPermissions_NullPermissions() {
        // When
        boolean result = permissionService.updateDynamicPermissions(null);

        // Then
        assertFalse(result);
        assertFalse(permissionService.hasDynamicPermissions());
    }

    @Test
    void testGetMergedPermissions_WithoutDynamic() {
        // When
        Map<String, List<String>> result = permissionService.getMergedPermissions();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get("admin")).containsExactly("user:create", "user:delete");
        assertThat(result.get("user")).containsExactly("user:read");
    }

    @Test
    void testGetMergedPermissions_WithDynamic() {
        // Given
        permissionService.updateDynamicPermissions(dynamicPermissions);

        // When
        Map<String, List<String>> result = permissionService.getMergedPermissions();

        // Then
        assertThat(result).hasSize(3); // admin, user, operator
        // 动态权限覆盖预置权限
        assertThat(result.get("admin")).containsExactly("user:create", "user:delete", "user:update");
        assertThat(result.get("operator")).containsExactly("user:read", "user:update");
    }

    @Test
    void testGetPermissionsByRole_WithDynamic() {
        // Given
        permissionService.updateDynamicPermissions(dynamicPermissions);

        // When & Then
        List<String> adminPermissions = permissionService.getPermissionsByRole("admin");
        assertThat(adminPermissions).containsExactly("user:create", "user:delete", "user:update");

        List<String> userPermissions = permissionService.getPermissionsByRole("user");
        assertThat(userPermissions).containsExactly("user:read");

        List<String> operatorPermissions = permissionService.getPermissionsByRole("operator");
        assertThat(operatorPermissions).containsExactly("user:read", "user:update");
    }

    @Test
    void testGetPermissionsByRole_WithoutDynamic() {
        // When & Then
        List<String> adminPermissions = permissionService.getPermissionsByRole("admin");
        assertThat(adminPermissions).containsExactly("user:create", "user:delete");

        List<String> nonExistentRole = permissionService.getPermissionsByRole("non-existent");
        assertNull(nonExistentRole);
    }

    @Test
    void testClearDynamicPermissions() {
        // Given
        permissionService.updateDynamicPermissions(dynamicPermissions);
        assertTrue(permissionService.hasDynamicPermissions());

        // When
        permissionService.clearDynamicPermissions();

        // Then
        assertFalse(permissionService.hasDynamicPermissions());
        Map<String, List<String>> result = permissionService.getMergedPermissions();
        assertThat(result).hasSize(2); // 只有预置权限
    }
}
