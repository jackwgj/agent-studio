/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session.impl;

import com.openjiuwen.studio.agent.space.app.model.session.SessionInfoDetail;
import com.openjiuwen.studio.agent.space.app.service.session.SessionRedisService;
import com.openjiuwen.studio.agent.space.app.service.session.SessionsManageService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * session管理服务类
 */
@Slf4j
@Component
public class SessionsManageServiceImpl implements SessionsManageService {
    @Resource
    private SessionRedisService sessionRedisService;

    @Override
    public SessionInfoDetail visitSession(String sessionId) {
        return querySessionDetail(sessionId);
    }

    public SessionInfoDetail querySessionDetail(String sessionId) {
        // 查询缓存
        return sessionRedisService.querySession(sessionId);
    }
}
