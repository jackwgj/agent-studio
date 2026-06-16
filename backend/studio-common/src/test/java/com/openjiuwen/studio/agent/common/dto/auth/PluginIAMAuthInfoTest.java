/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PluginIAMAuthInfoTest {

    @Test
    void testAllGettersSetters() {
        PluginIAMAuthInfo obj = new PluginIAMAuthInfo()
                .setIamUrl("http://iam.example.com")
                .setIamDomain("domain1")
                .setIamProject("project1")
                .setIamUser("user1")
                .setIamPassword("pass1")
                .setIamAk("ak-001")
                .setIamSk("sk-001");
        assertEquals("http://iam.example.com", obj.getIamUrl());
        assertEquals("domain1", obj.getIamDomain());
        assertEquals("project1", obj.getIamProject());
        assertEquals("user1", obj.getIamUser());
        assertEquals("pass1", obj.getIamPassword());
        assertEquals("ak-001", obj.getIamAk());
        assertEquals("sk-001", obj.getIamSk());
    }

    @Test
    void testEquals_SameObject() {
        PluginIAMAuthInfo obj = new PluginIAMAuthInfo().setIamUrl("http://iam");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        PluginIAMAuthInfo obj1 = new PluginIAMAuthInfo()
                .setIamUrl("http://iam").setIamDomain("d1")
                .setIamProject("p1").setIamUser("u1")
                .setIamPassword("pw1").setIamAk("ak").setIamSk("sk");
        PluginIAMAuthInfo obj2 = new PluginIAMAuthInfo()
                .setIamUrl("http://iam").setIamDomain("d1")
                .setIamProject("p1").setIamUser("u1")
                .setIamPassword("pw1").setIamAk("ak").setIamSk("sk");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        PluginIAMAuthInfo obj = new PluginIAMAuthInfo().setIamUrl("http://iam");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        PluginIAMAuthInfo obj = new PluginIAMAuthInfo().setIamUrl("http://iam");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        PluginIAMAuthInfo obj1 = new PluginIAMAuthInfo().setIamUrl("http://iam1");
        PluginIAMAuthInfo obj2 = new PluginIAMAuthInfo().setIamUrl("http://iam2");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        PluginIAMAuthInfo obj1 = new PluginIAMAuthInfo()
                .setIamUrl("http://iam").setIamDomain("d1");
        PluginIAMAuthInfo obj2 = new PluginIAMAuthInfo()
                .setIamUrl("http://iam").setIamDomain("d1");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        PluginIAMAuthInfo obj = new PluginIAMAuthInfo().setIamUrl("http://iam");
        assertNotNull(obj.toString());
    }
}
