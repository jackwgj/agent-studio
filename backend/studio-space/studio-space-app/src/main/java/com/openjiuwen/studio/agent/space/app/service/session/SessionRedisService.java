/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session;

import com.openjiuwen.studio.agent.space.app.model.session.SessionInfoDetail;

/**
 * redis服务类
 */
public interface SessionRedisService {
    String SESSION_PREFIX = "Agent_Session_Redis_";

    // 用户级token相关
    SessionInfoDetail querySession(String sessionId);

    void updateSession(SessionInfoDetail sessionInfo);
}