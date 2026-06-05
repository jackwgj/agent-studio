/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.config;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Data
@Configuration
@ConfigurationProperties(prefix = "system.permission")
public class PermissionProperties {

    private String obsRolePermissionUrl;
    private long refreshIntervalMs = 600000; // 默认10分钟，单位毫秒

    private static final Logger logger = LoggerFactory.getLogger(PermissionProperties.class);
    private boolean enabled = false;

    @PostConstruct
    public void init() {
        logger.info("=== PermissionProperties initialization ===");
        logger.info("obsRolePermissionUrl: {}", this.obsRolePermissionUrl);
        logger.info("refreshIntervalMs: {}", this.refreshIntervalMs);
        logger.info("enabled: {}", this.enabled);
        logger.info("================================");

        if (obsRolePermissionUrl == null || obsRolePermissionUrl.trim().isEmpty()) {
            logger.warn("obsRolePermissionUrl not configured");
        } else {
            logger.info("obsRolePermissionUrl normal configuration ");
        }
    }
}
