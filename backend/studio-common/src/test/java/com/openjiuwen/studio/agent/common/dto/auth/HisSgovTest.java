/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class HisSgovTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        HisSgov sgov = new HisSgov()
                .setAppId("app-001")
                .setCredential("cred-001")
                .setSgovUrl("https://sgov.example.com");
        assertEquals("app-001", sgov.getAppId());
        assertEquals("cred-001", sgov.getCredential());
        assertEquals("https://sgov.example.com", sgov.getSgovUrl());
    }

    @Test
    void testEquals_SameInstance() {
        HisSgov sgov = new HisSgov().setAppId("app");
        assertEquals(sgov, sgov);
    }

    @Test
    void testEquals_EqualObjects() {
        HisSgov s1 = new HisSgov().setAppId("app").setCredential("cred");
        HisSgov s2 = new HisSgov().setAppId("app").setCredential("cred");
        assertEquals(s1, s2);
    }

    @Test
    void testEquals_Null() {
        HisSgov sgov = new HisSgov();
        assertNotEquals(null, sgov);
    }

    @Test
    void testEquals_DifferentClass() {
        HisSgov sgov = new HisSgov();
        assertNotEquals("string", sgov);
    }

    @Test
    void testHashCode_Consistency() {
        HisSgov s1 = new HisSgov().setAppId("app").setCredential("cred");
        HisSgov s2 = new HisSgov().setAppId("app").setCredential("cred");
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        HisSgov sgov = new HisSgov().setAppId("app");
        assertNotNull(sgov.toString());
    }
}
