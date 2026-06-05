/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection.general;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;
import com.openjiuwen.studio.agent.foundation.connection.ConnectionParamType;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorParamDefinition;
import com.openjiuwen.studio.agent.foundation.connection.general.GeneralAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GeneralAuthHandlerTest {
    @InjectMocks
    private GeneralAuthHandler generalAuthHandler;

    private AutoCloseable mockitoCloseable;

    private static MockedStatic<CryptoUtils> mockedStaticCryptoUtils;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        mockedStaticCryptoUtils = mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();

        if (mockedStaticCryptoUtils != null) {
            mockedStaticCryptoUtils.close();
        }
    }

    @Test
    void test_generateAuthRequest_should_return_not_null_when_condition() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        ConnectorParamDefinition apiKeyParam = ConnectorParamDefinition.builder()
            .code("apiKey")
            .value("test-api-key")
            .build();
        paramDefinitions.add(apiKeyParam);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = mock(HttpMethod.class, Answers.RETURNS_DEEP_STUBS);
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_generateAuthRequest_should_set_authorization_header() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        ConnectorParamDefinition apiKeyParam = ConnectorParamDefinition.builder()
            .code("apiKey")
            .value("test-api-key")
            .build();
        paramDefinitions.add(apiKeyParam);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = HttpMethod.POST;
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
        assertEquals("Bearer test-api-key", result.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
    }

    @Test
    void test_generateAuthRequest_with_secret_apikey_should_decrypt_apikey() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        ConnectorParamDefinition apiKeyParam = ConnectorParamDefinition.builder()
            .code("apiKey")
            .value("encryptedApiKey")
            .type(ConnectionParamType.SECRET)
            .build();
        paramDefinitions.add(apiKeyParam);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = HttpMethod.POST;
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        mockedStaticCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decryptedApiKey");
        // When
        HttpEntity<Object> result = generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
        assertEquals("Bearer decryptedApiKey", result.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
    }

    @Test
    void test_generateAuthRequest_with_get_method_should_not_set_content_type() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        ConnectorParamDefinition apiKeyParam = ConnectorParamDefinition.builder()
            .code("apiKey")
            .value("test-api-key")
            .build();
        paramDefinitions.add(apiKeyParam);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
        assertEquals("Bearer test-api-key", result.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
        assertNull(result.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void test_generateAuthRequest_with_empty_apikey_should_not_set_authorization_header() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        ConnectorParamDefinition apiKeyParam = ConnectorParamDefinition.builder().code("apiKey").value("").build();
        paramDefinitions.add(apiKeyParam);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = HttpMethod.POST;
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getHeaders().get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void test_generateAuthRequest_with_null_request_entity() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        ConnectorParamDefinition apiKeyParam = ConnectorParamDefinition.builder()
            .code("apiKey")
            .value("test-api-key")
            .build();
        paramDefinitions.add(apiKeyParam);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = HttpMethod.POST;
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(null).build();

        // When
        assertThrows(AgentBaseException.class, () -> {
            generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);
        });
    }

    @Test
    void test_generateAuthRequest_with_missing_required_parameters() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition addressParam = ConnectorParamDefinition.builder()
            .code("endpoint")
            .value("http://test.com")
            .build();
        paramDefinitions.add(addressParam);
        // 缺少apiKey参数
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = HttpMethod.POST;
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = generalAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
        // 因为没有设置Authorization头，所以不会抛异常，但也不会设置头
        assertNull(result.getHeaders().get(HttpHeaders.AUTHORIZATION));
    }
}