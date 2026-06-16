/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.simple;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimpleUserTest {

    @Test
    void testSettersAndGetters() {
        SimpleUser user = new SimpleUser();
        user.setUserId("user-1");
        user.setToken("token-1");
        user.setUserName("name");
        user.setProjectId("proj-1");
        user.setDomainId("dom-1");
        user.setDomainName("dom-name");

        assertEquals("user-1", user.getUserId());
        assertEquals("token-1", user.getToken());
        assertEquals("name", user.getUserName());
        assertEquals("proj-1", user.getProjectId());
        assertEquals("dom-1", user.getDomainId());
        assertEquals("dom-name", user.getDomainName());
    }

    @Test
    void testDefaultValues() {
        SimpleUser user = new SimpleUser();
        assertEquals("0", user.getDomainId());
        assertEquals("0", user.getDomainName());
    }

    @Test
    void testNullFields() {
        SimpleUser user = new SimpleUser();
        assertNull(user.getUserId());
        assertNull(user.getToken());
        assertNull(user.getUserName());
        assertNull(user.getProjectId());
    }

    @Test
    void testSetDomainIdOverride() {
        SimpleUser user = new SimpleUser();
        user.setDomainId("custom-domain");
        assertEquals("custom-domain", user.getDomainId());
    }

    @Test
    void testSetDomainNameOverride() {
        SimpleUser user = new SimpleUser();
        user.setDomainName("custom-name");
        assertEquals("custom-name", user.getDomainName());
    }
}
