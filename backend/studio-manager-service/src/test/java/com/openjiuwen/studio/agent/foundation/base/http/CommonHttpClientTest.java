/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.base.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.model.KmsCryptoResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class CommonHttpClientTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.foundation.base.http.CommonHttpClient commonHttpClient;

    @Mock
    private SmartRestTemplate smartRestTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(commonHttpClient, "smartRestTemplate", smartRestTemplate);
    }

    /**
     * 测试 doRequest 正常执行
     */
    @Test
    void testDoRequestSuccess() {
        String url = "http://example.com/api/test";
        HttpMethod method = HttpMethod.GET;
        String requestBody = "mock-requestBody";
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", new ArrayList(Collections.singleton("application/json")));

        RequestEntity requestEntity = RequestEntity.builder()
            .host("http://example.com")
            .uri("/api/test")
            .method(method)
            .body(requestBody)
            .headers(headers)
            .build();

        KmsCryptoResponse response = new KmsCryptoResponse();
        response.setPlainText("mock-plainText");
        ResponseEntity<KmsCryptoResponse> responseEntity = ResponseEntity.ok(response);

        when(smartRestTemplate.exchange(any(), any(), any(), eq(KmsCryptoResponse.class))).thenReturn(responseEntity);

        KmsCryptoResponse result = commonHttpClient.doRequest(requestEntity, KmsCryptoResponse.class);

        assertEquals(response, result);
    }

    @Test
    void testDoRequestFailure() {
        String url = "http://example.com/api/test";
        HttpMethod method = HttpMethod.POST;
        String requestBody = "mock-requestBody";
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", new ArrayList(Collections.singleton("application/json")));

        RequestEntity requestEntity = RequestEntity.builder()
            .host("http://example.com")
            .uri("/api/test")
            .method(method)
            .body(requestBody)
            .headers(headers)
            .build();

        // 模拟请求失败
        when(smartRestTemplate.exchange(any(), any(), any(), eq(KmsCryptoResponse.class))).thenThrow(
            new RestClientException("网络异常"));

        assertThrows(AgentBaseException.class, () -> {
            commonHttpClient.doRequest(requestEntity, KmsCryptoResponse.class);
        });
    }

    @Test
    void testDoRequestWithEmptyParams() {
        String url = "http://example.com/api/test";
        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = new HttpHeaders();

        RequestEntity requestEntity = RequestEntity.builder()
            .host("http://example.com")
            .uri("/api/test")
            .method(method)
            .headers(headers)
            .build();

        KmsCryptoResponse response = new KmsCryptoResponse();
        response.setPlainText("mock-plainText");
        ResponseEntity<KmsCryptoResponse> responseEntity = ResponseEntity.ok(response);

        when(smartRestTemplate.exchange(any(), any(), any(), eq(KmsCryptoResponse.class))).thenReturn(responseEntity);

        KmsCryptoResponse result = commonHttpClient.doRequest(requestEntity, KmsCryptoResponse.class);

        assertEquals(response, result);
    }

    @Test
    void testDoRequestWithPathVariable() {
        String url = "http://example.com/api/test/{id}";
        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = new HttpHeaders();

        Map<String, Object> pathVariables = new HashMap<>();
        pathVariables.put("id", "12345");
        Map<String, Object> requestVariables = new HashMap<>();
        requestVariables.put("mock-request", "mock-requestVariables");

        RequestEntity requestEntity = RequestEntity.builder()
            .host("http://example.com")
            .uri("/api/test/{id}")
            .method(method)
            .headers(headers)
            .pathVariables(pathVariables)
            .requestParams(requestVariables)
            .build();

        KmsCryptoResponse response = new KmsCryptoResponse();
        response.setPlainText("mock-plainText");
        ResponseEntity<KmsCryptoResponse> responseEntity = ResponseEntity.ok(response);

        when(smartRestTemplate.exchange(any(), any(), any(), eq(KmsCryptoResponse.class))).thenReturn(responseEntity);

        KmsCryptoResponse result = commonHttpClient.doRequest(requestEntity, KmsCryptoResponse.class);

        assertEquals(response, result);
    }

    @Test
    void testDoRequestWithEmptyHeaders() {
        String url = "http://example.com/api/test";
        HttpMethod method = HttpMethod.POST;
        String requestBody = "mock-requestBody";

        RequestEntity requestEntity = RequestEntity.builder()
            .host("http://example.com")
            .uri("/api/test")
            .method(method)
            .body(requestBody)
            .build();

        KmsCryptoResponse response = new KmsCryptoResponse();
        response.setPlainText("mock-plainText");
        ResponseEntity<KmsCryptoResponse> responseEntity = ResponseEntity.ok(response);

        when(smartRestTemplate.exchange(any(), any(), any(), eq(KmsCryptoResponse.class))).thenReturn(responseEntity);

        KmsCryptoResponse result = commonHttpClient.doRequest(requestEntity, KmsCryptoResponse.class);

        assertEquals(response, result);
    }

    @Test
    void testDoRequestWithNullBody() {
        String url = "http://example.com/api/test";
        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = new HttpHeaders();

        RequestEntity requestEntity = RequestEntity.builder()
            .host("http://example.com")
            .uri("/api/test")
            .method(method)
            .headers(headers)
            .body(null)
            .build();

        KmsCryptoResponse response = new KmsCryptoResponse();
        response.setPlainText("mock-plainText");
        ResponseEntity<KmsCryptoResponse> responseEntity = ResponseEntity.ok(response);

        when(smartRestTemplate.exchange(any(), any(), any(), eq(KmsCryptoResponse.class))).thenReturn(responseEntity);

        KmsCryptoResponse result = commonHttpClient.doRequest(requestEntity, KmsCryptoResponse.class);

        assertEquals(response, result);
    }
}
