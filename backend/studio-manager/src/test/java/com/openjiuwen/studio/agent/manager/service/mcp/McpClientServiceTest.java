/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

/**
 * @description
 * @since 2025/8/8
 **/
@Transactional
public class McpClientServiceTest extends BaseTest {
    @InjectMocks
    private McpClientService mcpClientService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        Set<String> blackProxyHost = new HashSet<>();
        blackProxyHost.add("127.0.0.1");
        ReflectionTestUtils.setField(mcpClientService, "proxyOpen", false);
        ReflectionTestUtils.setField(mcpClientService, "proxyUser", "not_empty");
        ReflectionTestUtils.setField(mcpClientService, "proxyPassword", "not_empty");
        ReflectionTestUtils.setField(mcpClientService, "proxyPort", 0);
        ReflectionTestUtils.setField(mcpClientService, "proxyHost", "not_empty");
        ReflectionTestUtils.setField(mcpClientService, "blackProxyHost", blackProxyHost);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @BeforeAll
    public static void init() {

    }

    @AfterAll
    static void end() {
    }

    @Test
    void testGetMcpServiceToolList() {
        Map<String, String> header = new HashMap<>();
        header.put("x-hw", "test");
        McpClientService mcpClientService = new McpClientService();
        assertNotNull(mcpClientService.buildMcpClient("test", "http://localhost:8444/sse/", "sse", header));
        assertNotNull(mcpClientService.buildMcpClient("test", "http://localhost:8444/test/", "sse", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> mcpClientService.buildMcpClient("test",
            "://localhost:8444/test/", "sse", null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> mcpClientService.buildMcpClient("test",
            "://localhost:8444/test/", "streamable_http", null));
    }

    @Test
    public void test_getBuilder_with_proxy_closed() throws Exception {
        // Given
        ReflectionTestUtils.setField(mcpClientService, "proxyOpen", true);
        SSLContext sslContext = SSLContext.getDefault();
        String url = "http://localhost:8444/test/";

        // When
        HttpClient.Builder builder = mcpClientService.getBuilder(sslContext, url);

        // Then
        assertNotNull(builder);
        assertNotNull(Authenticator.getDefault());
    }

    @Test
    void test_getBuilder_should_return_not_null() throws Exception {
        // Given
        SSLContext sslContext = mock(SSLContext.class, Answers.RETURNS_DEEP_STUBS);
        String url = "http://localhost:8444/test/";

        // When
        HttpClient.Builder result = mcpClientService.getBuilder(sslContext, url);

        // Then
        assertNotNull(result);
    }
}
