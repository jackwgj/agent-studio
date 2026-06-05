/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.MockitoAnnotations.openMocks;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.dto.run.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.runtime.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationResp;
import com.openjiuwen.studio.agent.runtime.mcp.McpServerClient;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteServer;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteTools;
import com.openjiuwen.studio.agent.runtime.mcp.models.McpRemoteValidation;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import io.modelcontextprotocol.spec.McpSchema;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 */
public class McpServerRuntimeServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServerClient mcpClient;

    private McpServerRuntimeService mcpServerRuntimeService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    public void setUp() throws Exception {
        mockitoCloseable = openMocks(this);
        mcpServerRuntimeService = new McpServerRuntimeService(mcpClient);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    public void test_call_tool_should_return_not_null_when_test_data_combination() throws Exception {
        try (
            MockedStatic<RequestContextUtils> mockedRequestContextUtils =
                mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<CryptoUtils> mockedSccCryptoUtils = mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS)) {

            mockedRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("userId");
            mockedRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");
            mockedSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypt string");

            McpSchema.CallToolResult callToolResult = new McpSchema.CallToolResult("call resp", false);
            Mockito.when(mcpClient.callTool(anyString(), anyString(), anyMap(), anyString(), anyMap()))
                .thenReturn(callToolResult);

            AuthInfo authInfo = new AuthInfo().setScope(AuthInfo.ScopeEnum.SERVICE)
                .setDomain(AuthInfo.DomainEnum.HEADERS)
                .setAuthKeys(List.of(new AuthKeyInfo().setTargetName("token").setAuthKey("tokenValue")));

            McpCallToolReq body = new McpCallToolReq();
            body.setName("method");
            body.setParams(Map.of("key", "value"));
            body.setUrl("https://fake.com/sse");
            body.setAuth(authInfo);

            // run the test
            McpCallToolResp result = mcpServerRuntimeService.callTool("projectId", body);

            // verify the results
            assertFalse(result.isIsError());
        }
    }

    @Test
    public void test_test_server_should_return_not_null_when_test_data_combination() throws Exception {
        // setup
        McpRemoteValidation mcpServerValidation = new McpRemoteValidation(true, "string");
        Mockito.when(mcpClient.checkServerValid(anyString(), anyString(), anyMap())).thenReturn(mcpServerValidation);

        McpValidationReq body = new McpValidationReq();
        body.setUrl("https://fake.com");

        AuthInfo authInfo = new AuthInfo().setScope(AuthInfo.ScopeEnum.SERVICE)
            .setDomain(AuthInfo.DomainEnum.QUERY)
            .setAuthKeys(List.of(new AuthKeyInfo().setTargetName("token").setAuthKey("123")));
        body.setAuth(authInfo);

        // run the test
        McpValidationResp result = mcpServerRuntimeService.testServer("string", body);

        // verify the results
        assertNotNull(result);
    }

    @Test
    public void test_list_mcp_server_tools_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<CryptoUtils> mockedSccCryptoUtils = mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS)) {
            mockedSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("123");
            // setup
            McpRemoteServer mcpRemoteServer = new McpRemoteServer();
            McpRemoteTools tools1 = new McpRemoteTools();
            mcpRemoteServer.setTools(tools1);
            Mockito.when(mcpClient.getRemoteServerInfo(anyString(), anyString(), anyMap())).thenReturn(mcpRemoteServer);

            McpValidationReq body = new McpValidationReq();
            body.setUrl("https://fake.com");
            body.setAuth(new AuthInfo().setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setAuthKey("123")))
                .setDomain(AuthInfo.DomainEnum.QUERY)
                .setScope(AuthInfo.ScopeEnum.SERVICE));

            // run the test
            Object result = mcpServerRuntimeService.listMcpServerTools("string", body);

            // verify the results
            assertNotNull(result);
        }
    }
}
