/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.exception;

import com.openjiuwen.studio.agent.common.enums.StudioError;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AgentStudioExceptionTest {

    @Test
    void testConstructor_WithErrorCodeAndArgs() {
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, "arg1", "arg2");
        assertEquals(StudioError.UNEXPECTED_ERROR, ex.getErrorCode());
        assertArrayEquals(new Object[]{"arg1", "arg2"}, ex.getArgs());
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructor_WithErrorCodeAndMessage() {
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, "specific error");
        assertEquals(StudioError.UNEXPECTED_ERROR, ex.getErrorCode());
        assertEquals("specific error", ex.getMessage());
        assertArrayEquals(new Object[]{"specific error"}, ex.getArgs());
    }

    @Test
    void testConstructor_WithErrorCodeAndDetails() {
        List<String> details = Arrays.asList("detail1", "detail2");
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, details, "arg1");
        assertEquals(StudioError.UNEXPECTED_ERROR, ex.getErrorCode());
        assertEquals(details, ex.getDetails());
        assertArrayEquals(new Object[]{"arg1"}, ex.getArgs());
    }

    @Test
    void testConstructor_WithErrorCodeAndException() {
        RuntimeException cause = new RuntimeException("original");
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, cause, "arg1");
        assertEquals(StudioError.UNEXPECTED_ERROR, ex.getErrorCode());
        assertSame(cause, ex.getCause());
        assertArrayEquals(new Object[]{"arg1"}, ex.getArgs());
    }

    @Test
    void testConstructor_WithStringMessage() {
        AgentStudioException ex = new AgentStudioException("simple message");
        assertEquals(StudioError.UNEXPECTED_ERROR, ex.getErrorCode());
        assertEquals("simple message", ex.getMessage());
    }

    @Test
    void testSetErrorCode() {
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, "test");
        ex.setErrorCode(StudioError.NAME_FORMAT);
        assertEquals(StudioError.NAME_FORMAT, ex.getErrorCode());
    }

    @Test
    void testSetDetails() {
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, "test");
        assertNull(ex.getDetails());
        List<String> details = List.of("d1", "d2");
        ex.setDetails(details);
        assertEquals(details, ex.getDetails());
    }

    @Test
    void testConstructor_WithNoArgs() {
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        assertEquals(StudioError.UNEXPECTED_ERROR, ex.getErrorCode());
        assertNotNull(ex.getArgs());
        assertEquals(0, ex.getArgs().length);
    }

    @Test
    void testConstructor_WithEmptyDetails() {
        List<String> details = List.of();
        AgentStudioException ex = new AgentStudioException(StudioError.UNEXPECTED_ERROR, details);
        assertEquals(details, ex.getDetails());
    }

    @Test
    void testIsRuntimeException() {
        AgentStudioException ex = new AgentStudioException("test");
        assert ex instanceof RuntimeException;
    }
}
