/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.net.BindException;
import java.sql.SQLException;
import java.util.jar.JarException;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpServiceExceptionUtilsTest {

    @Test
    void testPrintException_WithSensitiveException() {
        Logger logger = mock(Logger.class);
        FileNotFoundException exception = new FileNotFoundException("secret path");
        McpServiceExceptionUtils.printException(logger, exception, "test log");
        verify(logger).error(org.mockito.ArgumentMatchers.contains("test log"), org.mockito.ArgumentMatchers.any(Object.class));
    }

    @Test
    void testPrintException_WithJarException() {
        Logger logger = mock(Logger.class);
        JarException exception = new JarException("jar error");
        McpServiceExceptionUtils.printException(logger, exception, "test log");
        verify(logger).error(org.mockito.ArgumentMatchers.contains("test log"), org.mockito.ArgumentMatchers.any(Object.class));
    }

    @Test
    void testPrintException_WithBindException() {
        Logger logger = mock(Logger.class);
        BindException exception = new BindException("bind error");
        McpServiceExceptionUtils.printException(logger, exception, "test log");
        verify(logger).error(org.mockito.ArgumentMatchers.contains("test log"), org.mockito.ArgumentMatchers.any(Object.class));
    }

    @Test
    void testPrintException_WithSQLException() {
        Logger logger = mock(Logger.class);
        SQLException exception = new SQLException("sql error");
        McpServiceExceptionUtils.printException(logger, exception, "test log");
        verify(logger).error(org.mockito.ArgumentMatchers.contains("test log"), org.mockito.ArgumentMatchers.any(Object.class));
    }

    @Test
    void testPrintException_WithNonSensitiveException() {
        Logger logger = mock(Logger.class);
        RuntimeException exception = new RuntimeException("runtime error");
        McpServiceExceptionUtils.printException(logger, exception, "test log");
        verify(logger).error(org.mockito.ArgumentMatchers.contains("test log"), org.mockito.ArgumentMatchers.eq(exception));
    }

    @Test
    void testThrowRemoteCallException() {
        assertThrows(AgentStudioException.class,
            () -> McpServiceExceptionUtils.throwRemoteCallException("MCP", "error msg"));
    }

    @Test
    void testThrowUserNoPermissionError() {
        assertThrows(AgentStudioException.class,
            () -> McpServiceExceptionUtils.throwUserNoPermissionError());
    }

    @Test
    void testThrowMissingParameterError() {
        assertThrows(AgentStudioException.class,
            () -> McpServiceExceptionUtils.throwMissingParameterError("paramName"));
    }

    @Test
    void testThrowMissingParameterError_WithNull() {
        assertThrows(AgentStudioException.class,
            () -> McpServiceExceptionUtils.throwMissingParameterError(null));
    }

    @Test
    void testThrowRemoteCallException_WithNullType() {
        assertThrows(AgentStudioException.class,
            () -> McpServiceExceptionUtils.throwRemoteCallException(null, null));
    }
}
