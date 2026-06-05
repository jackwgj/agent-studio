/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.config;// PermissionLoader.java

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.manager.entity.RolePermission;
import com.openjiuwen.studio.agent.manager.service.PermissionService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PermissionLoader implements CommandLineRunner {

    private final Map<String, List<String>> roleAndPermission = new HashMap<>();

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        // 加载JSON文件
        Resource resource = resourceLoader.getResource("classpath:role_permissions.json");
        Resource resourceInterceptor = resourceLoader.getResource("classpath:role_permissions_interceptor.json");

        // 解析JSON到对象列表
        RolePermission[] rolePermissions = JsonUtils.json2Obj(resource.getInputStream(), new TypeReference<>() { });

        if (rolePermissions != null) {
            for (RolePermission rp : rolePermissions) {
                roleAndPermission.put(rp.getRoleName(), rp.getPermissions());
            }
        }

        // 两种方式暂时共存，后续只解析role_permissions_interceptor.json
        RolePermission[] rolePermissionsInterceptor = JsonUtils.json2Obj(resourceInterceptor.getInputStream(), new TypeReference<>() { });

        if (rolePermissionsInterceptor != null) {
            for (RolePermission rp : rolePermissionsInterceptor) {
                if (roleAndPermission.containsKey(rp.getRoleName())) {
                    List<String> permissions = roleAndPermission.get(rp.getRoleName());
                    permissions.addAll(rp.getPermissions());
                } else {
                    roleAndPermission.put(rp.getRoleName(), rp.getPermissions());
                }
            }
        }

        permissionService.setPresetPermissions(roleAndPermission);
    }

    public Map<String, List<String>> getRoleAndPermission() {

        return roleAndPermission;
    }
}
