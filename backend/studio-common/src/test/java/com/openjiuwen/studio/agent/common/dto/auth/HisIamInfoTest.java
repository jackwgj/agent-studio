/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class HisIamInfoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        HisIamInfo info = new HisIamInfo()
                .setIamUrl("https://iam.example.com")
                .setIamAccount("account1")
                .setIamProject("project1")
                .setIamSecret("secret1")
                .setIamEnterprise("enterprise1");
        assertEquals("https://iam.example.com", info.getIamUrl());
        assertEquals("account1", info.getIamAccount());
        assertEquals("project1", info.getIamProject());
        assertEquals("secret1", info.getIamSecret());
        assertEquals("enterprise1", info.getIamEnterprise());
    }

    @Test
    void testEquals_SameInstance() {
        HisIamInfo info = new HisIamInfo().setIamUrl("url");
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObjects() {
        HisIamInfo i1 = new HisIamInfo().setIamUrl("url").setIamAccount("acc");
        HisIamInfo i2 = new HisIamInfo().setIamUrl("url").setIamAccount("acc");
        assertEquals(i1, i2);
    }

    @Test
    void testEquals_Null() {
        HisIamInfo info = new HisIamInfo();
        assertNotEquals(null, info);
    }

    @Test
    void testEquals_DifferentClass() {
        HisIamInfo info = new HisIamInfo();
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        HisIamInfo i1 = new HisIamInfo().setIamUrl("url").setIamAccount("acc");
        HisIamInfo i2 = new HisIamInfo().setIamUrl("url").setIamAccount("acc");
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        HisIamInfo info = new HisIamInfo().setIamUrl("url");
        assertNotNull(info.toString());
    }
}
