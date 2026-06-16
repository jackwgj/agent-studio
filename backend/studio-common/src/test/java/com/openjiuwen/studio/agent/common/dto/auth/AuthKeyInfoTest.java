/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AuthKeyInfoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        AuthKeyInfo info = new AuthKeyInfo()
                .setTargetName("target1")
                .setSourceName("source1")
                .setAuthKey("key1")
                .setDesc("description1");
        assertEquals("target1", info.getTargetName());
        assertEquals("source1", info.getSourceName());
        assertEquals("key1", info.getAuthKey());
        assertEquals("description1", info.getDesc());
    }

    @Test
    void testEquals_SameInstance() {
        AuthKeyInfo info = new AuthKeyInfo().setTargetName("t");
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObjects() {
        AuthKeyInfo a1 = new AuthKeyInfo().setTargetName("t").setAuthKey("k");
        AuthKeyInfo a2 = new AuthKeyInfo().setTargetName("t").setAuthKey("k");
        assertEquals(a1, a2);
    }

    @Test
    void testEquals_Null() {
        AuthKeyInfo info = new AuthKeyInfo();
        assertNotEquals(null, info);
    }

    @Test
    void testEquals_DifferentClass() {
        AuthKeyInfo info = new AuthKeyInfo();
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        AuthKeyInfo a1 = new AuthKeyInfo().setTargetName("t").setAuthKey("k");
        AuthKeyInfo a2 = new AuthKeyInfo().setTargetName("t").setAuthKey("k");
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AuthKeyInfo info = new AuthKeyInfo().setTargetName("t");
        assertNotNull(info.toString());
    }
}
