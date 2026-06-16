/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.mcp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.net.ssl.SSLContext;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class McpClientServiceTest {

    private McpClientService mcpClientService;

    @BeforeEach
    void setUp() {
        mcpClientService = new McpClientService();
        ReflectionTestUtils.setField(mcpClientService, "proxyOpen", false);
        ReflectionTestUtils.setField(mcpClientService, "proxyUser", "");
        ReflectionTestUtils.setField(mcpClientService, "proxyPassword", "");
        ReflectionTestUtils.setField(mcpClientService, "proxyPort", 8080);
        ReflectionTestUtils.setField(mcpClientService, "proxyHost", "");
        ReflectionTestUtils.setField(mcpClientService, "blackProxyHost", Set.of("127.0.0.1"));
    }

    @Test
    void testBuildMcpClient_BlankBaseUrl() {
        var result = mcpClientService.buildMcpClient("svc-1", "", "SSE", Collections.emptyMap());
        assertNull(result);
    }

    @Test
    void testBuildMcpClient_NullBaseUrl() {
        var result = mcpClientService.buildMcpClient("svc-1", null, "SSE", Collections.emptyMap());
        assertNull(result);
    }

    @Test
    void testBuildMcpClient_SseType() {
        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080/sse", "SSE",
            Collections.emptyMap());
        assertNotNull(result);
    }

    @Test
    void testBuildMcpClient_StreamableHttpType() {
        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080/mcp",
            "streamable_http", Collections.emptyMap());
        assertNotNull(result);
    }

    @Test
    void testBuildMcpClient_WithCustomHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("X-Custom", "value");

        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080/sse", "SSE", headers);
        assertNotNull(result);
    }

    @Test
    void testBuildMcpClient_TrailingSlashRemoved() {
        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080/", "SSE",
            Collections.emptyMap());
        assertNotNull(result);
    }

    @Test
    void testBuildMcpClient_NullHeaderValueFiltered() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Valid-Header", "value");
        headers.put(null, "null-key-value");

        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080/sse", "SSE", headers);
        assertNotNull(result);
    }

    @Test
    void testGetBuilder_ProxyDisabled() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, SecureRandom.getInstanceStrong());

        HttpClient.Builder builder = mcpClientService.getBuilder(sslContext, "http://example.com");

        assertNotNull(builder);
    }

    @Test
    void testGetBuilder_ProxyEnabledBlackHost() throws NoSuchAlgorithmException, KeyManagementException {
        ReflectionTestUtils.setField(mcpClientService, "proxyOpen", true);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, SecureRandom.getInstanceStrong());

        HttpClient.Builder builder = mcpClientService.getBuilder(sslContext, "http://127.0.0.1:8080");

        assertNotNull(builder);
    }

    @Test
    void testGetBuilder_ProxyEnabledNonBlackHost() throws NoSuchAlgorithmException, KeyManagementException {
        ReflectionTestUtils.setField(mcpClientService, "proxyOpen", true);
        ReflectionTestUtils.setField(mcpClientService, "proxyHost", "proxy.example.com");
        ReflectionTestUtils.setField(mcpClientService, "proxyUser", "user");
        ReflectionTestUtils.setField(mcpClientService, "proxyPassword", "pass");
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, SecureRandom.getInstanceStrong());

        HttpClient.Builder builder = mcpClientService.getBuilder(sslContext, "http://external.com");

        assertNotNull(builder);
    }

    @Test
    void testBuildMcpClient_StreamableHttpWithCustomPath() {
        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080/custom/path",
            "streamable_http", Collections.emptyMap());
        assertNotNull(result);
    }

    @Test
    void testBuildMcpClient_StreamableHttpRootPath() {
        var result = mcpClientService.buildMcpClient("svc-1", "http://localhost:8080",
            "streamable_http", Collections.emptyMap());
        assertNotNull(result);
    }
}
