/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorInfoTest {

    @Test
    void testNoArgsConstructor() {
        ErrorInfo errorInfo = new ErrorInfo();
        assertNotNull(errorInfo);
        assertNull(errorInfo.getMessage());
        assertNull(errorInfo.getReason());
        assertNull(errorInfo.getSuggestion());
    }

    @Test
    void testAllArgsConstructor() {
        ErrorInfo errorInfo = new ErrorInfo("msg", "reason", "suggestion");
        assertEquals("msg", errorInfo.getMessage());
        assertEquals("reason", errorInfo.getReason());
        assertEquals("suggestion", errorInfo.getSuggestion());
    }

    @Test
    void testSetters() {
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setMessage("m");
        errorInfo.setReason("r");
        errorInfo.setSuggestion("s");
        assertEquals("m", errorInfo.getMessage());
        assertEquals("r", errorInfo.getReason());
        assertEquals("s", errorInfo.getSuggestion());
    }

    @Test
    void testConstructorWithNulls() {
        ErrorInfo errorInfo = new ErrorInfo(null, null, null);
        assertNull(errorInfo.getMessage());
        assertNull(errorInfo.getReason());
        assertNull(errorInfo.getSuggestion());
    }
}
