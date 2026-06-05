/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.config;

import com.openjiuwen.studio.agent.manager.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 会话清理定时任务配置
 */
@Configuration
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class SessionCleanupConfig {

    private final SessionService sessionService;

    @Value("${app.auth.session.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * 定时清理过期会话
     * 默认每天执行一次
     */
    @Scheduled(fixedRateString = "#{${app.auth.session.cleanup.interval:86400} * 1000}")
    public void cleanupExpiredSessions() {
        if (!cleanupEnabled) {
            log.debug("The session cleanup feature has been disabled, skipping the cleanup task");
            return;
        }

        try {
            log.info("Starting the session cleanup task...");
            sessionService.cleanupExpiredSessions();
            log.info("Session cleanup task completed    ");
        } catch (Exception e) {
            log.error("Session cleanup task execution failed", e);
        }
    }

}
