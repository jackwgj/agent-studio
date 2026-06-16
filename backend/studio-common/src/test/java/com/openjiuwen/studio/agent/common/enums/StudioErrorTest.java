/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioErrorTest {

    @Test
    void testValues() {
        assertTrue(StudioError.values().length > 0);
    }

    @Test
    void testUnexpectedError() {
        StudioError error = StudioError.UNEXPECTED_ERROR;
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        assertEquals(StudioError.Module.COMMON, error.getModule());
        assertEquals("1002", error.getCode());
    }

    @Test
    void testGetFullCode() {
        String fullCode = StudioError.UNEXPECTED_ERROR.getFullCode();
        assertEquals("openjiuwen.02001002", fullCode);
    }

    @Test
    void testModuleSubCode() {
        assertEquals("0200", StudioError.Module.COMMON.getSubCode());
        assertEquals("0210", StudioError.Module.AGENT.getSubCode());
        assertEquals("0220", StudioError.Module.WORKFLOW.getSubCode());
        assertEquals("0230", StudioError.Module.MULTI_AGENT.getSubCode());
        assertEquals("0240", StudioError.Module.COMPONENT.getSubCode());
        assertEquals("0250", StudioError.Module.MODEL.getSubCode());
        assertEquals("0300", StudioError.Module.KNOWLEDGE_BASE.getSubCode());
    }

    @Test
    void testAllErrorsHaveNonNullFields() {
        for (StudioError error : StudioError.values()) {
            assertNotNull(error.getHttpStatus());
            assertNotNull(error.getModule());
            assertNotNull(error.getCode());
        }
    }

    @Test
    void testModuleValues() {
        assertTrue(StudioError.Module.values().length > 0);
    }

    @Test
    void testMethodArgumentNotValid() {
        StudioError error = StudioError.METHOD_ARGUMENT_NOT_VALID;
        assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        assertEquals("1003", error.getCode());
    }
}
