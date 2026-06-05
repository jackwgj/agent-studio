/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.apache.hc.client5.http.classic.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
class HttpClientConfigTest {

    @Mock
    private HttpClientPoolConfig httpClientPoolConfig;

    @Mock
    private UserProxyConfig userProxyConfig;

    @InjectMocks
    private com.openjiuwen.studio.agent.foundation.base.http.HttpClientConfig httpClientConfig;

    @BeforeEach
    void setUp() {
        // 使用 lenient 避免 UnnecessaryStubbingException
        lenient().when(httpClientPoolConfig.getMaxTotalConnect()).thenReturn(10);
        lenient().when(httpClientPoolConfig.getMaxConnectPerRoute()).thenReturn(5);
        lenient().when(httpClientPoolConfig.getConnectTimeout()).thenReturn(1000);
        lenient().when(httpClientPoolConfig.getConnectionRequestTimout()).thenReturn(500);
        lenient().when(httpClientPoolConfig.getRetryTimes()).thenReturn(2);
        lenient().when(httpClientPoolConfig.getCharset()).thenReturn(StandardCharsets.UTF_8.name());

        lenient().when(userProxyConfig.isEnabled()).thenReturn(true);
        lenient().when(userProxyConfig.getHost()).thenReturn("localhost");
        lenient().when(userProxyConfig.getPort()).thenReturn(8080);
        lenient().when(userProxyConfig.getUsername()).thenReturn("user");
        lenient().when(userProxyConfig.getPassword()).thenReturn("pwd");
        ReflectionTestUtils.setField(httpClientConfig, "sslEnabled", false);
    }

    @Test
    void testRestTemplateForCloudService() {
        RestTemplate restTemplate = httpClientConfig.restTemplateForCloudService();
        assertNotNull(restTemplate);
    }

    @Test
    void testRestTemplateForCloudServiceWithProxy_enabled() {
        RestTemplate restTemplate = httpClientConfig.restTemplateForCloudServiceWithProxy(userProxyConfig);
        assertNotNull(restTemplate);
    }

    @Test
    void testRestTemplateForCloudServiceWithProxy_disabled() {
        when(userProxyConfig.isEnabled()).thenReturn(false);
        RestTemplate restTemplate = httpClientConfig.restTemplateForCloudServiceWithProxy(userProxyConfig);
        assertNotNull(restTemplate);
    }

    @Test
    void testCheckHttpClientPoolConfig_invalid() {
        // 测 maxTotalConnect 异常
        when(httpClientPoolConfig.getMaxTotalConnect()).thenReturn(0);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> httpClientConfig.restTemplateForCloudService());
        assertTrue(ex1.getMessage().contains("invalid maxTotalConnection"));

        // 测 maxConnectPerRoute 异常
        when(httpClientPoolConfig.getMaxTotalConnect()).thenReturn(10);
        when(httpClientPoolConfig.getMaxConnectPerRoute()).thenReturn(0);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> httpClientConfig.restTemplateForCloudService());
        assertTrue(ex2.getMessage().contains("invalid maxConnectionPerRoute"));
    }

    @Test
    void testHttpClientForCloudService() {
        HttpClient client = httpClientConfig.httpClientForCloudService();
        assertNotNull(client);
    }
}
