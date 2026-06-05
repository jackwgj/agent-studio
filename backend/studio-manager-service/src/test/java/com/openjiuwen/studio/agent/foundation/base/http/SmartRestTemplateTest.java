/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SmartRestTemplateTest {

    @Mock
    private RestTemplate restTemplateDirect;

    @Mock
    private RestTemplate restTemplateProxy;

    @Mock
    private UserProxyConfig userProxyConfig;

    @InjectMocks
    private SmartRestTemplate smartRestTemplate;

    @BeforeEach
    void setUp() {
        // 默认配置
        when(userProxyConfig.isEnabled()).thenReturn(true);
        when(userProxyConfig.getHost()).thenReturn("mock.proxy.local");
        when(userProxyConfig.getPort()).thenReturn(1080);
        when(userProxyConfig.getSchema()).thenReturn("http");

        // mock RestTemplate 返回值
        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplateDirect.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(mockResponse);
        when(restTemplateProxy.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(mockResponse);
    }

    @Test
    void testAllBranches() {
        // 1. proxy disabled
        when(userProxyConfig.isEnabled()).thenReturn(false);
        smartRestTemplate.exchange("http://example.com", HttpMethod.GET, null, String.class);
        verify(restTemplateDirect, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));

        // 2. invalid URI
        when(userProxyConfig.isEnabled()).thenReturn(true);
        smartRestTemplate.exchange("http://a b c", HttpMethod.GET, null, String.class);
        verify(restTemplateDirect, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));

        // 3. host in noProxy
        Set<String> noProxy = new HashSet<>();
        noProxy.add("example.com");
        when(userProxyConfig.getNoProxy()).thenReturn(noProxy);
        smartRestTemplate.exchange("http://example.com/path", HttpMethod.GET, null, String.class);
        verify(restTemplateDirect, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));

        // 4. host not in noProxy → use proxy
        noProxy.clear();
        noProxy.add("127.0.0.1");
        when(userProxyConfig.getNoProxy()).thenReturn(noProxy);
        smartRestTemplate.exchange("http://other.com/path", HttpMethod.GET, null, String.class);
        verify(restTemplateProxy, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));

        // 5. host == null (e.g. mailto)
        when(userProxyConfig.getNoProxy()).thenReturn(new HashSet<>());
        smartRestTemplate.exchange("mailto:test@example.com", HttpMethod.GET, null, String.class);
        verify(restTemplateDirect, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));

        // 6. noProxy = null
        when(userProxyConfig.getNoProxy()).thenReturn(null);
        smartRestTemplate.exchange("http://another.com", HttpMethod.GET, null, String.class);
        verify(restTemplateProxy, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));
    }

}