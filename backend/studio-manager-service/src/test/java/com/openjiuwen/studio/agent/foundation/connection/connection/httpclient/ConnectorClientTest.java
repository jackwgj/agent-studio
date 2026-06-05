/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.foundation.base.http.SmartRestTemplate;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ConnectorClientTest {

    @Mock
    private SmartRestTemplate smartRestTemplate;

    @InjectMocks
    private ConnectorClient connectorClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoRequest_CombinedScenarios() {
        // 准备测试数据
        BasicRequest request = BasicRequest.builder()
            .host("https://api.example.com")
            .uri("resource/{id}")
            .method(HttpMethod.GET)
            .build();
        // 情况1: 成功响应
        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(smartRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(String.class))).thenReturn(successResponse);

        // 情况2: 异常抛出
        doThrow(new RestClientException("Network Error")).when(smartRestTemplate)
            .exchange(eq("https://api.example.com/resource/123?param1=value1"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class));

        // 构造请求实体
        RequestEntity requestEntity = RequestEntity.builder()
            .pathVariables(Collections.singletonMap("id", "123"))
            .requestParams(Collections.singletonMap("param1", "value1"))
            .build();
        request.setRequestEntity(requestEntity);
        HttpEntity<Object> entity = new HttpEntity<>(new HttpHeaders());

        // 场景1: 成功调用
        String result = connectorClient.doRequest(request, entity, String.class);
        assertEquals("Success", result);

    }

    @Test
    void testBuildCompleteUrl_CombinedScenarios() {
        // 测试数据准备
        BasicRequest request = BasicRequest.builder()
            .host("https://api.example.com")
            .uri("resource/{id}")
            .method(HttpMethod.GET)
            .build();

        // 情况1: 完整参数

        Map<String, Object> fullParams = new HashMap<>();
        fullParams.put("q", "test");
        fullParams.put("page", 2);
        fullParams.put("empty", "");
        RequestEntity fullParamsEntity = RequestEntity.builder()
            .pathVariables(Collections.singletonMap("id", "123"))
            .requestParams(fullParams)
            .build();
        request.setRequestEntity(fullParamsEntity);

        // 情况2: 空参数

        RequestEntity emptyEntity = RequestEntity.builder()
            .pathVariables(Collections.emptyMap())
            .requestParams(Collections.emptyMap())
            .build();

        BasicRequest emptyRequest = BasicRequest.builder()
            .requestEntity(emptyEntity)
            .uri("/base")
            .host("https://api.example.com")
            .build();
        // 执行测试
        String fullUrl = ConnectorClient.buildCompleteUrl(request);
        String emptyUrl = ConnectorClient.buildCompleteUrl(emptyRequest);

        assert fullUrl.equals("https://api.example.comresource/123?q=test&page=2");
        assert emptyUrl.equals("https://api.example.com/base");

    }
}
