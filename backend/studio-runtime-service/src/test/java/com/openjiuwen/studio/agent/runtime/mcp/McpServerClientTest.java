/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteServer;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteValidation;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 */
public class McpServerClientTest {
    private static final String MCP_URL = "https://fake.com/sse";

    private McpServerClient mcpServerClient;

    @BeforeEach
    public void setUp() throws Exception {
        mcpServerClient = new McpServerClient();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void test_get_remote_server_info() throws Exception {
        try (MockedStatic<McpClient> mockedStaticMcpClient = mockStatic(McpClient.class, RETURNS_DEEP_STUBS)) {

            McpSyncClient syncClient = Mockito.mock(McpSyncClient.class);

            // setup
            mockedStaticMcpClient.when(() -> McpClient.sync(any(HttpClientSseClientTransport.class))
                .requestTimeout(any(Duration.class))
                .capabilities(any(ClientCapabilities.class))
                .build()).thenReturn(syncClient);

            McpSchema.Implementation serverInfo = new McpSchema.Implementation("test server", "1.0");
            when(syncClient.getServerInfo()).thenReturn(serverInfo);

            List<McpSchema.Tool> tools = new ArrayList<>();
            tools.add(McpSchema.Tool.builder()
                .name("test tool")
                .description("test tool desc")
                // 需要把字符串 "{}" 转成 JsonSchema 对象
                .inputSchema(new ObjectMapper().readValue("{}", McpSchema.JsonSchema.class))
                .build());
            McpSchema.ListToolsResult listToolsResult = new McpSchema.ListToolsResult(tools, null);
            when(syncClient.listTools()).thenReturn(listToolsResult);

            // run the test
            McpRemoteServer result = mcpServerClient.getRemoteServerInfo("https://fake.com:8080/sse", null, null);

            assertEquals(result.getName(), "test server");
            assertEquals(result.getVersion(), "1.0");
            assertEquals(result.getTools().getTools().size(), 1);
        }
    }

    @Test
    public void test_check_remote_server_info() throws Exception {
        try (MockedStatic<McpClient> mockedStaticMcpClient = mockStatic(McpClient.class, RETURNS_DEEP_STUBS)) {

            McpSyncClient syncClient = Mockito.mock(McpSyncClient.class);

            // setup
            mockedStaticMcpClient.when(() -> McpClient.sync(any(HttpClientSseClientTransport.class))
                .requestTimeout(any(Duration.class))
                .capabilities(any(ClientCapabilities.class))
                .build()).thenReturn(syncClient);

            // run the test
            McpRemoteValidation result = mcpServerClient.checkServerValid("https://fake.com/sse", null, null);
            assertTrue(result.isSuccess());
        }
    }

    @Test
    public void test_call_tool_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<McpClient> mockedStaticMcpClient = mockStatic(McpClient.class, RETURNS_DEEP_STUBS)) {
            McpSyncClient syncClient = Mockito.mock(McpSyncClient.class);

            // setup
            mockedStaticMcpClient.when(() -> McpClient.sync(any(HttpClientSseClientTransport.class))
                .requestTimeout(any(Duration.class))
                .capabilities(any(ClientCapabilities.class))
                .build()).thenReturn(syncClient);

            McpSchema.Implementation serverInfo = new McpSchema.Implementation("test server", "1.0");
            when(syncClient.getServerInfo()).thenReturn(serverInfo);

            List<McpSchema.Tool> tools = new ArrayList<>();
            tools.add(McpSchema.Tool.builder()
                .name("test tool")
                .description("test tool desc")
                // 需要把字符串 "{}" 转成 JsonSchema 对象
                .inputSchema(new ObjectMapper().readValue("{}", McpSchema.JsonSchema.class))
                .build());
            McpSchema.ListToolsResult listToolsResult = new McpSchema.ListToolsResult(tools, null);
            when(syncClient.listTools()).thenReturn(listToolsResult);

            // setup
            Map<String, String> headers = new HashMap<>();
            headers.put("string", "string");

            Map<String, Object> params = new HashMap<>();
            Object object = new Object();
            params.put("string", object);

            // 使用 Builder 构建对象
            CallToolResult mockResult = McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent("11"))) // 设置返回内容
                .isError(true)                                     // 设置是否出错
                .build();

            when(syncClient.callTool(any())).thenReturn(mockResult);

            // run the test
            CallToolResult result = mcpServerClient.callTool(MCP_URL, "string", headers, "testTool", params);

            // verify the results
            assertNotNull(result);

            result = mcpServerClient.callTool(MCP_URL, "string", headers, "testToolNotFind", params);
            // verify the result
            assertTrue(result.isError());

            when(syncClient.callTool(any()))
                .thenThrow(new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(-111, "mock error", null)));

            result = mcpServerClient.callTool(MCP_URL, "string", headers, "testTool", params);
            // verify the result
            assertTrue(result.isError());
        }
    }
}
