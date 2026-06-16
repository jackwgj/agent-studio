/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KooSearchAuthModeTest {

    @Test
    void testEnumValues() {
        KooSearchAuthMode[] values = KooSearchAuthMode.values();
        assertEquals(3, values.length);
    }

    @Test
    void testNone() {
        assertEquals("none", KooSearchAuthMode.NONE.getAuthMode());
    }

    @Test
    void testAppCode() {
        assertEquals("app_code", KooSearchAuthMode.APP_CODE.getAuthMode());
    }

    @Test
    void testToken() {
        assertEquals("token", KooSearchAuthMode.TOKEN.getAuthMode());
    }

    @Test
    void testValueOf() {
        assertEquals(KooSearchAuthMode.NONE, KooSearchAuthMode.valueOf("NONE"));
        assertEquals(KooSearchAuthMode.APP_CODE, KooSearchAuthMode.valueOf("APP_CODE"));
        assertEquals(KooSearchAuthMode.TOKEN, KooSearchAuthMode.valueOf("TOKEN"));
    }
}
