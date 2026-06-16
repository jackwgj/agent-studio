/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class OauthInfoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        OauthInfo info = new OauthInfo()
                .setEndpointUrl("https://oauth.example.com")
                .setClientId("client1")
                .setClientSecret("secret1")
                .setScope("read");
        assertEquals("https://oauth.example.com", info.getEndpointUrl());
        assertEquals("client1", info.getClientId());
        assertEquals("secret1", info.getClientSecret());
        assertEquals("read", info.getScope());
    }

    @Test
    void testEquals_SameInstance() {
        OauthInfo info = new OauthInfo().setEndpointUrl("url");
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObjects() {
        OauthInfo o1 = new OauthInfo().setEndpointUrl("url").setClientId("c1");
        OauthInfo o2 = new OauthInfo().setEndpointUrl("url").setClientId("c1");
        assertEquals(o1, o2);
    }

    @Test
    void testEquals_Null() {
        OauthInfo info = new OauthInfo();
        assertNotEquals(null, info);
    }

    @Test
    void testEquals_DifferentClass() {
        OauthInfo info = new OauthInfo();
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        OauthInfo o1 = new OauthInfo().setEndpointUrl("url").setClientId("c1");
        OauthInfo o2 = new OauthInfo().setEndpointUrl("url").setClientId("c1");
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        OauthInfo info = new OauthInfo().setEndpointUrl("url");
        assertNotNull(info.toString());
    }
}
