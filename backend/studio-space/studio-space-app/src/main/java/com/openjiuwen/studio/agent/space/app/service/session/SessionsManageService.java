/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session;

import com.openjiuwen.studio.agent.space.app.model.session.SessionInfoDetail;

/**
 * session管理服务类
 */
public interface SessionsManageService {
    SessionInfoDetail visitSession(String sessionId);
}
