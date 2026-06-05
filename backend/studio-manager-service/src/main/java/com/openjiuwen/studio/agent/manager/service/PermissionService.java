/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    // 平台预置权限缓存 - 保持原来的数据结构
    private final Map<String, List<String>> presetRolePermissions = new ConcurrentHashMap<>();

    // 动态权限缓存（会覆盖预置权限） - 保持原来的数据结构
    private final Map<String, List<String>> dynamicRolePermissions = new ConcurrentHashMap<>();

    /**
     * 更新动态权限 - 添加格式验证和错误处理
     */
    public boolean updateDynamicPermissions(Map<String, List<String>> permissions) {
        try {
            // 验证权限格式
            if (!validatePermissionFormat(permissions)) {
                logger.error("Dynamic permission format validation failed");
                return false;
            }

            dynamicRolePermissions.clear();
            dynamicRolePermissions.putAll(permissions);
            logger.info("Dynamic permissions updated successfully, roles: {}", permissions.keySet());
            return true;
        } catch (Exception e) {
            logger.error("Error updating dynamic permissions, keeping current permissions", e);
            return false;
        }
    }

    /**
     * 验证权限数据格式
     */
    private boolean validatePermissionFormat(Map<String, List<String>> permissions) {
        if (permissions == null) {
            logger.warn("Permissions map is null");
            return false;
        }

        for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
            String role = entry.getKey();
            List<String> permissionList = entry.getValue();

            if (role == null || role.trim().isEmpty()) {
                logger.warn("Invalid role name found: {}", role);
                return false;
            }

            if (permissionList == null) {
                logger.warn("Permission list is null for role: {}", role);
                return false;
            }

            // 检查权限列表中的每个权限是否合法
            for (String permission : permissionList) {
                if (permission == null || permission.trim().isEmpty()) {
                    logger.warn("Invalid permission found for role {}: {}", role, permission);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 获取最终权限（动态权限覆盖预置权限）
     */
    public Map<String, List<String>> getMergedPermissions() {
        Map<String, List<String>> merged = new HashMap<>(presetRolePermissions);
        merged.putAll(dynamicRolePermissions);
        return merged;
    }

    /**
     * 获取特定角色的权限列表
     */
    public List<String> getPermissionsByRole(String role) {
        // 先查动态权限，如果没有则查预置权限
        if (dynamicRolePermissions.containsKey(role)) {
            return dynamicRolePermissions.get(role);
        }
        return presetRolePermissions.get(role);
    }

    /**
     * 清空动态权限（回退到预置权限）
     */
    public void clearDynamicPermissions() {
        dynamicRolePermissions.clear();
        logger.info("Dynamic permissions cleared, using preset permissions");
    }

    public boolean hasDynamicPermissions() {
        return !dynamicRolePermissions.isEmpty();
    }

    /**
     * 获取当前动态权限（用于调试和监控）
     */
    public Map<String, List<String>> getCurrentDynamicPermissions() {
        return new HashMap<>(dynamicRolePermissions);
    }

    /**
     * 获取平台预置权限（用于调试和监控）
     */
    public Map<String, List<String>> getPresetPermissions() {
        return new HashMap<>(presetRolePermissions);
    }

    /**
     * 设置平台预置权限
     */
    public void setPresetPermissions(Map<String, List<String>> permissions) {
        presetRolePermissions.clear();
        presetRolePermissions.putAll(permissions);
        logger.info("Platform preset permissions loaded, roles: {}", permissions.keySet());
    }
}
