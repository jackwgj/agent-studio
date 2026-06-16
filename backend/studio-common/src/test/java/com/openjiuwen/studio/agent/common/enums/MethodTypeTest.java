/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodTypeTest {

    @Test
    void testValues() {
        assertEquals(4, MethodType.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(MethodType.GET, MethodType.valueOf("GET"));
        assertEquals(MethodType.POST, MethodType.valueOf("POST"));
        assertEquals(MethodType.DELETE, MethodType.valueOf("DELETE"));
        assertEquals(MethodType.PUT, MethodType.valueOf("PUT"));
    }
}
