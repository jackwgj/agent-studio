/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService.clearDynamicPermissions();
    }

    @Test
    void testUpdateDynamicPermissions_Success() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("admin", List.of("read", "write"));
        boolean result = permissionService.updateDynamicPermissions(permissions);
        assertTrue(result);
        assertTrue(permissionService.hasDynamicPermissions());
    }

    @Test
    void testUpdateDynamicPermissions_NullPermissions() {
        boolean result = permissionService.updateDynamicPermissions(null);
        assertFalse(result);
    }

    @Test
    void testUpdateDynamicPermissions_NullRoleName() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put(null, List.of("read"));
        boolean result = permissionService.updateDynamicPermissions(permissions);
        assertFalse(result);
    }

    @Test
    void testUpdateDynamicPermissions_EmptyRoleName() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("  ", List.of("read"));
        boolean result = permissionService.updateDynamicPermissions(permissions);
        assertFalse(result);
    }

    @Test
    void testUpdateDynamicPermissions_NullPermissionList() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("admin", null);
        boolean result = permissionService.updateDynamicPermissions(permissions);
        assertFalse(result);
    }

    @Test
    void testUpdateDynamicPermissions_NullPermissionInList() {
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put("admin", List.of("read", null));
        boolean result = permissionService.updateDynamicPermissions(permissions);
        assertFalse(result);
    }

    @Test
    void testGetMergedPermissions_NoDynamic() {
        Map<String, List<String>> preset = new HashMap<>();
        preset.put("user", List.of("read"));
        permissionService.setPresetPermissions(preset);

        Map<String, List<String>> merged = permissionService.getMergedPermissions();
        assertNotNull(merged);
        assertEquals(1, merged.size());
        assertTrue(merged.containsKey("user"));
    }

    @Test
    void testGetMergedPermissions_DynamicOverridesPreset() {
        Map<String, List<String>> preset = new HashMap<>();
        preset.put("admin", List.of("read"));
        permissionService.setPresetPermissions(preset);

        Map<String, List<String>> dynamic = new HashMap<>();
        dynamic.put("admin", List.of("read", "write", "delete"));
        permissionService.updateDynamicPermissions(dynamic);

        Map<String, List<String>> merged = permissionService.getMergedPermissions();
        assertEquals(List.of("read", "write", "delete"), merged.get("admin"));
    }

    @Test
    void testGetPermissionsByRole_FromDynamic() {
        Map<String, List<String>> dynamic = new HashMap<>();
        dynamic.put("admin", List.of("read", "write"));
        permissionService.updateDynamicPermissions(dynamic);

        List<String> perms = permissionService.getPermissionsByRole("admin");
        assertEquals(List.of("read", "write"), perms);
    }

    @Test
    void testGetPermissionsByRole_FromPreset() {
        Map<String, List<String>> preset = new HashMap<>();
        preset.put("user", List.of("read"));
        permissionService.setPresetPermissions(preset);

        List<String> perms = permissionService.getPermissionsByRole("user");
        assertEquals(List.of("read"), perms);
    }

    @Test
    void testGetPermissionsByRole_NotExist() {
        List<String> perms = permissionService.getPermissionsByRole("nonexistent");
        assertNull(perms);
    }

    @Test
    void testClearDynamicPermissions() {
        Map<String, List<String>> dynamic = new HashMap<>();
        dynamic.put("admin", List.of("read"));
        permissionService.updateDynamicPermissions(dynamic);
        assertTrue(permissionService.hasDynamicPermissions());

        permissionService.clearDynamicPermissions();
        assertFalse(permissionService.hasDynamicPermissions());
    }

    @Test
    void testHasDynamicPermissions_Empty() {
        assertFalse(permissionService.hasDynamicPermissions());
    }

    @Test
    void testGetCurrentDynamicPermissions() {
        Map<String, List<String>> dynamic = new HashMap<>();
        dynamic.put("admin", List.of("read"));
        permissionService.updateDynamicPermissions(dynamic);

        Map<String, List<String>> current = permissionService.getCurrentDynamicPermissions();
        assertNotNull(current);
        assertEquals(1, current.size());
    }

    @Test
    void testGetPresetPermissions() {
        Map<String, List<String>> preset = new HashMap<>();
        preset.put("user", List.of("read"));
        permissionService.setPresetPermissions(preset);

        Map<String, List<String>> result = permissionService.getPresetPermissions();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSetPresetPermissions() {
        Map<String, List<String>> preset = new HashMap<>();
        preset.put("admin", List.of("all"));
        preset.put("user", List.of("read"));
        permissionService.setPresetPermissions(preset);

        Map<String, List<String>> result = permissionService.getPresetPermissions();
        assertEquals(2, result.size());
    }
}
