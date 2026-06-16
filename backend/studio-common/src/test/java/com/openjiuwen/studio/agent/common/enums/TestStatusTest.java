/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestStatusTest {

    @Test
    void testValues() {
        assertEquals(3, TestStatus.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(TestStatus.SUCCESS, TestStatus.valueOf("SUCCESS"));
        assertEquals(TestStatus.FAILED, TestStatus.valueOf("FAILED"));
        assertEquals(TestStatus.UNKNOWN, TestStatus.valueOf("UNKNOWN"));
    }

    @Test
    void testFromCode_Success() {
        assertEquals(TestStatus.SUCCESS, TestStatus.fromCode(0));
    }

    @Test
    void testFromCode_Failed() {
        assertEquals(TestStatus.FAILED, TestStatus.fromCode(1));
    }

    @Test
    void testFromCode_Unknown() {
        assertEquals(TestStatus.UNKNOWN, TestStatus.fromCode(2));
    }

    @Test
    void testFromCode_Null() {
        assertEquals(TestStatus.UNKNOWN, TestStatus.fromCode(null));
    }

    @Test
    void testFromCode_InvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> TestStatus.fromCode(99));
    }

    @Test
    void testGetCode() {
        assertEquals(0, TestStatus.SUCCESS.getCode());
        assertEquals(1, TestStatus.FAILED.getCode());
        assertEquals(2, TestStatus.UNKNOWN.getCode());
    }
}
