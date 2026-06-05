/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.manager.service.mcp.McpServiceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

/**
 * McpServiceManager 私有方法 matchErrorByMessage 的单元测试
 */
public class mcpTest {

    private McpServiceManager mcpServiceManager;
    private Method matchErrorByMessageMethod;

    @BeforeEach
    public void setUp() throws Exception {
        // 1. 实例化目标类 (由于该方法不依赖注入的Bean，直接 new 即可)
        mcpServiceManager = new McpServiceManager();

        // 2. 获取私有方法对象
        // 方法签名: matchErrorByMessage(String errorMsg, StudioError defaultError)
        matchErrorByMessageMethod = McpServiceManager.class.getDeclaredMethod(
                "matchErrorByMessage", String.class, StudioError.class);

        // 3. 设置为可访问 (暴力反射关键步骤)
        matchErrorByMessageMethod.setAccessible(true);
    }

    /**
     * 辅助调用私有方法的包装器
     */
    private StudioError invokeMatchError(String errorMsg, StudioError defaultError) throws Exception {
        return (StudioError) matchErrorByMessageMethod.invoke(mcpServiceManager, errorMsg, defaultError);
    }

    @Test
    @DisplayName("测试空消息 - 应返回默认错误")
    public void testMatchErrorByMessage_Blank() throws Exception {
        StudioError defaultError = StudioError.MCP_FETCH_TOOLS_SERVER_ERROR;

        assertEquals(defaultError, invokeMatchError(null, defaultError));
        assertEquals(defaultError, invokeMatchError("", defaultError));
        assertEquals(defaultError, invokeMatchError("   ", defaultError));
    }

    @ParameterizedTest
    @DisplayName("测试网络超时匹配 (Timeout/timed out)")
    @CsvSource({
            "Read timed out",
            "Connection timed out",
            "Request Timeout",
            "SocketTimeoutException: connect timed out"
    })
    public void testMatchErrorByMessage_Timeout(String errorMsg) throws Exception {
        StudioError result = invokeMatchError(errorMsg, StudioError.MCP_FETCH_TOOLS_SERVER_ERROR);
        assertEquals(StudioError.MCP_NETWORK_TIMEOUT, result);
    }

    @ParameterizedTest
    @DisplayName("测试连接拒绝匹配 (Connection refused)")
    @CsvSource({
            "Connection refused: connect",
            "java.net.ConnectException: Connection refused"
    })
    public void testMatchErrorByMessage_ConnectionRefused(String errorMsg) throws Exception {
        StudioError result = invokeMatchError(errorMsg, StudioError.MCP_FETCH_TOOLS_SERVER_ERROR);
        assertEquals(StudioError.MCP_CONNECTION_REFUSED, result);
    }

    @ParameterizedTest
    @DisplayName("测试 404 错误匹配")
    @CsvSource({
            "HTTP 404 Not Found",
            "Server returned HTTP response code: 404 for URL"
    })
    public void testMatchErrorByMessage_404(String errorMsg) throws Exception {
        StudioError result = invokeMatchError(errorMsg, StudioError.MCP_FETCH_TOOLS_SERVER_ERROR);
        assertEquals(StudioError.MCP_TARGET_NOT_FOUND, result);
    }

    @ParameterizedTest
    @DisplayName("测试认证失败匹配 (401/403)")
    @CsvSource({
            "HTTP 401 Unauthorized",
            "HTTP 403 Forbidden",
            "Server returned HTTP response code: 401"
    })
    public void testMatchErrorByMessage_AuthFailed(String errorMsg) throws Exception {
        StudioError result = invokeMatchError(errorMsg, StudioError.MCP_FETCH_TOOLS_SERVER_ERROR);
        assertEquals(StudioError.MCP_AUTHENTICATION_FAILED, result);
    }

    @Test
    @DisplayName("测试未知主机匹配 (UnknownHost)")
    public void testMatchErrorByMessage_UnknownHost() throws Exception {
        String errorMsg = "java.net.UnknownHostException: non-existent-domain.com";
        StudioError result = invokeMatchError(errorMsg, StudioError.MCP_FETCH_TOOLS_SERVER_ERROR);
        assertEquals(StudioError.MCP_UNKNOWN_HOST, result);
    }

    @Test
    @DisplayName("测试未匹配到的普通错误 - 应返回默认错误")
    public void testMatchErrorByMessage_Unmatched() throws Exception {
        String errorMsg = "Internal Server Error 500"; // 代码里没写500的逻辑
        StudioError defaultError = StudioError.MCP_FETCH_TOOLS_SERVER_ERROR;

        StudioError result = invokeMatchError(errorMsg, defaultError);
        assertEquals(defaultError, result);
    }
}
