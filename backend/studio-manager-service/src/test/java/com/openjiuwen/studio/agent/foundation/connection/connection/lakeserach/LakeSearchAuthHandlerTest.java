/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection.lakeserach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorParamDefinition;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;
import com.openjiuwen.studio.agent.foundation.connection.lakeserach.LakeSearchAuthHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class LakeSearchAuthHandlerTest {
    @InjectMocks
    private LakeSearchAuthHandler lakeSearchAuthHandler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_generateAuthRequest_should_return_not_null_when_condition() throws Exception {
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition connectorParamDefinition = ConnectorParamDefinition.builder().code("address").build();
        paramDefinitions.add(connectorParamDefinition);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = mock(HttpMethod.class, Answers.RETURNS_DEEP_STUBS);
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = lakeSearchAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_generateAuthRequest_should_return_not_null_when_use_safe_mode() throws Exception {
        // Given
        List<ConnectorParamDefinition> paramDefinitions = new ArrayList<>();
        ConnectorParamDefinition connectorParamDefinition = ConnectorParamDefinition.builder().code("endpoint").build();
        paramDefinitions.add(connectorParamDefinition);
        ConnectorParamDefinition connectorParamDefinitionUserName = ConnectorParamDefinition.builder()
            .code("user_name")
            .value("userName")
            .build();
        paramDefinitions.add(connectorParamDefinitionUserName);
        ConnectorParamDefinition connectorParamDefinitionPwd = ConnectorParamDefinition.builder()
            .code("password")
            .value("password")
            .build();
        paramDefinitions.add(connectorParamDefinitionPwd);
        ConnectorDefinition connectorDefinition = ConnectorDefinition.builder()
            .paramDefinitions(paramDefinitions)
            .build();

        HttpMethod method = mock(HttpMethod.class, Answers.RETURNS_DEEP_STUBS);
        HttpHeaders headers = new HttpHeaders();
        RequestEntity requestEntity = RequestEntity.builder().headers(headers).body("string").build();
        BasicRequest basicRequest = BasicRequest.builder().method(method).requestEntity(requestEntity).build();

        // When
        HttpEntity<Object> result = lakeSearchAuthHandler.generateAuthRequest(connectorDefinition, basicRequest);

        // Then
        assertNotNull(result);
        byte[] userPwd = Base64.getDecoder()
            .decode(result.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Basic ", ""));
        assertEquals("userName:password", new String(userPwd, Charset.defaultCharset()));
    }

    @Test
    void test_generateAuthRequest_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            HttpEntity<Object> result = lakeSearchAuthHandler.generateAuthRequest(null, null);
        });
    }
}