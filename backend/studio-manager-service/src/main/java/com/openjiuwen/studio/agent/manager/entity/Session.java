/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体类 - 用于数据库存储会话记录
 */
@Entity
@Table(name = "t_sessions")
@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "login_time")
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "last_activity_time")
    private LocalDateTime lastActivityTime = LocalDateTime.now();

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "domain_id")
    private String domainId;

    @Column(name = "project_id")
    private String projectId;

    @PreUpdate
    public void preUpdate() {
        this.lastActivityTime = LocalDateTime.now();
    }

    // 检查会话是否活跃（基于状态和过期时间）
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE && this.expireTime != null
            && LocalDateTime.now().isBefore(this.expireTime);
    }

    // 检查会话是否因超时而过期（基于最后活动时间）
    public boolean isTimedOut(long timeoutSeconds) {
        if (this.lastActivityTime == null) {
            return true;
        }
        LocalDateTime timeoutTime = this.lastActivityTime.plusSeconds(timeoutSeconds);
        return LocalDateTime.now().isAfter(timeoutTime);
    }

    public enum SessionStatus {
        ACTIVE,      // 活跃
        EXPIRED,     // 过期
        LOGGED_OUT   // 已登出
    }
}
