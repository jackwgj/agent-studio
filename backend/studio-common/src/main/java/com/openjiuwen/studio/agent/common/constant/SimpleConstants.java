/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.constant;

public interface SimpleConstants {
    String AGENT_SID = "AGENT_SID";

    String AUTH_TOKEN_URI = "/v3/auth/tokens";

    interface AuthType {
        String NORMAL = "normal";

        String SIMPLE = "simple";
    }

    interface UserInfo {
        String USER_FIELD = "X-USER-ID";

        String PROJECT_FIELD = "X-PROJECT-ID";
    }

    /**
     * X_AUTH_TOKEN常量
     */
    String X_AUTH_TOKEN = "X-Auth-Token";

    /**
     * contextPath参数名
     */
    String SERVLET_CONTEXT_PATH = "server.servlet.context-path";

}
