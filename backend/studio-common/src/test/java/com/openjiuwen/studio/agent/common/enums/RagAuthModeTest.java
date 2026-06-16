/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RagAuthModeTest {

    @Test
    void testValues() {
        assertEquals(3, RagAuthMode.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(RagAuthMode.NONE, RagAuthMode.valueOf("NONE"));
        assertEquals(RagAuthMode.BASIC, RagAuthMode.valueOf("BASIC"));
        assertEquals(RagAuthMode.KERBEROS, RagAuthMode.valueOf("KERBEROS"));
    }

    @Test
    void testToString() {
        assertEquals("none", RagAuthMode.NONE.toString());
        assertEquals("basic", RagAuthMode.BASIC.toString());
        assertEquals("kerberos", RagAuthMode.KERBEROS.toString());
    }
}
