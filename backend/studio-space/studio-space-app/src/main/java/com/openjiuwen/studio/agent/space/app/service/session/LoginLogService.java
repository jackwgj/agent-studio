/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session;

import com.openjiuwen.studio.agent.space.dao.entity.LoginLogEntity;

import java.util.List;

public interface LoginLogService {
    void createLoginLog(LoginLogEntity loginLog);

    void logOutLoginLog(String sessionId, String logoutType, String content);

    void batchLogOutLoginLog(List<String> sessionIds, String logoutType);
}
