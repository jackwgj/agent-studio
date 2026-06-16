/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SimpleConstantsTest {

    @Test
    void testAgentSid() {
        assertEquals("AGENT_SID", SimpleConstants.AGENT_SID);
    }

    @Test
    void testAuthTokenUri() {
        assertEquals("/v3/auth/tokens", SimpleConstants.AUTH_TOKEN_URI);
    }

    @Test
    void testXAuthToken() {
        assertEquals("X-Auth-Token", SimpleConstants.X_AUTH_TOKEN);
    }

    @Test
    void testServletContextPath() {
        assertEquals("server.servlet.context-path", SimpleConstants.SERVLET_CONTEXT_PATH);
    }

    @Test
    void testAuthTypeNormal() {
        assertEquals("normal", SimpleConstants.AuthType.NORMAL);
    }

    @Test
    void testAuthTypeSimple() {
        assertEquals("simple", SimpleConstants.AuthType.SIMPLE);
    }

    @Test
    void testUserInfoUserField() {
        assertEquals("X-USER-ID", SimpleConstants.UserInfo.USER_FIELD);
    }

    @Test
    void testUserInfoProjectField() {
        assertEquals("X-PROJECT-ID", SimpleConstants.UserInfo.PROJECT_FIELD);
    }
}
