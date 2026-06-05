/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.rce.client.ApigClient;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;
import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthRelations;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;
import com.huaweicloud.sdk.apig.v2.model.CreateApiGroupV2Response;
import com.huaweicloud.sdk.apig.v2.model.CreateApiV2Response;
import com.huaweicloud.sdk.apig.v2.model.DeleteApiV2Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ApigApiServiceTest {

    @InjectMocks
    private ApigApiService apigApiService;

    @Mock
    private ApigRequestService apigRequestService;

    @Mock
    private ApigClient apigClient;

    @Mock
    private IamServiceUtils iamServiceUtils;

    private HttpHeaders headers;

    private String projectId;

    private String instanceId;

    private String tenantId;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        projectId = "test-project-id";
        instanceId = "test-instance-id";
        tenantId = "test-tenant-id";
        ReflectionTestUtils.setField(apigApiService, "instanceId", "instanceId");
        ReflectionTestUtils.setField(apigApiService, "projectId", "projectId");
    }

    @Test
    void testCreateApi_Success() {
        // Arrange
        ApiCreate request = new ApiCreate();
        request.setName("test-api");

        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("x-auth-token", "test-token");

        CreateApiV2Response expectedResponse = new CreateApiV2Response();
        expectedResponse.setId("test-api-id");

        ResponseEntity<CreateApiV2Response> responseEntity = ResponseEntity.ok(expectedResponse);
        when(apigClient.createApi(anyString(), anyString(), anyString(), any(ApiCreate.class))).thenReturn(
            responseEntity);
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("token");
        // Act
        CreateApiV2Response result = apigApiService.createApi(request);

        // Assert
        assertNotNull(result);
        assertEquals("test-api-id", result.getId());
    }

    @Test
    void testCreateApi_ExceptionThrown() {
        // Arrange
        ApiCreate request = new ApiCreate();
        request.setName("test-api");

        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("x-auth-token", "test-token");

        when(apigClient.createApi(anyString(), anyString(), anyString(), any(ApiCreate.class))).thenThrow(
            new AgentStudioException("Connection failed"));

        // Act & Assert
        assertThrows(AgentStudioException.class, () -> {
            apigApiService.createApi(request);
        });
    }

    @Test
    void testDeleteApi_Success() {
        // Arrange
        String apiId = "test-api-id";
        DeleteApiV2Response expectedResponse = new DeleteApiV2Response();
        expectedResponse.setHttpStatusCode(HttpStatus.NO_CONTENT.value());

        ResponseEntity<DeleteApiV2Response> responseEntity = ResponseEntity.ok(expectedResponse);
        when(apigClient.deleteApi(anyString(), anyString(), anyString(), anyString())).thenReturn(responseEntity)
            .thenReturn(responseEntity);
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("token");

        // Act
        ResponseEntity<DeleteApiV2Response> result = apigApiService.deleteApi(apiId);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT.value(), result.getBody().getHttpStatusCode());
    }

    @Test
    void testDeleteApi_ExceptionThrown() {
        // Arrange
        String apiId = "test-api-id";

        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("x-auth-token", "test-token");

        when(apigClient.deleteApi(anyString(), anyString(), anyString(), anyString())).thenThrow(
            new AgentStudioException("Connection failed"));
        // Act & Assert
        assertThrows(AgentStudioException.class, () -> {
            apigApiService.deleteApi("app_id");
        });
    }

    @Test
    void testCreateApiGroup_Success() {
        // Arrange
        ApiGroupCreate request = new ApiGroupCreate();
        request.setName("test-group");

        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("x-auth-token", "test-token");

        CreateApiGroupV2Response expectedResponse = new CreateApiGroupV2Response();
        expectedResponse.setId("test-group-id");

        ResponseEntity<CreateApiGroupV2Response> responseEntity = ResponseEntity.ok(expectedResponse);
        when(apigClient.createApiGroup(anyString(), anyString(), anyString(), any(ApiGroupCreate.class))).thenReturn(
            responseEntity);
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("token");
        // Act
        CreateApiGroupV2Response result = apigApiService.createApiGroup(request);

        // Assert
        assertNotNull(result);
        assertEquals("test-group-id", result.getId());
    }

    @Test
    void testCreateApiGroup_ExceptionThrown() {
        // Arrange
        ApiGroupCreate request = new ApiGroupCreate();
        request.setName("test-group");

        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("x-auth-token", "test-token");

        when(apigClient.createApiGroup(anyString(), anyString(), anyString(), any(ApiGroupCreate.class))).thenThrow(
            new AgentStudioException("Connection failed"));

        // Act & Assert
        assertThrows(AgentStudioException.class, () -> {
            apigApiService.createApiGroup(request);
        });
    }

    @Test
    void testPublishOrOfflineApi_Success() {
        // Arrange
        ApiActionInfo request = new ApiActionInfo();
        request.setApiId("test-api-id");
        request.setAction(ApiActionInfo.ActionEnum.ONLINE);

        Map<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put("x-auth-token", "test-token");

        // Act
        apigApiService.publishOrOfflineApi(request);
        assertEquals("test-api-id", request.getApiId());
    }

    @Test
    void testApiBindCredentials_Success() {
        // Arrange
        ApiAuthCreate apiAuthCreate = new ApiAuthCreate();
        apiAuthCreate.setApiIds(java.util.Arrays.asList("api1", "api2"));

        HttpHeaders mockHeaders = new HttpHeaders();
        mockHeaders.add("x-auth-token", "test-token");

        ApiAuthRelations expectedResponse = new ApiAuthRelations();
        expectedResponse.setApiId("test-api-id");

        ResponseEntity<String> responseEntity = ResponseEntity.ok("{\"envId\":\"envId\",\"api_id\":\"test-api-id\"}");
        when(apigClient.bindCredentials(any(), anyString(), anyString(), any(ApiAuthCreate.class))).thenReturn(
            responseEntity);

        // Act
        ApiAuthRelations result = apigApiService.apiBindCredentials(apiAuthCreate);

        // Assert
        assertNotNull(result);
        assertEquals("test-api-id", result.getApiId());
    }

    @Test
    void testApiBindCredentials_RestClientException() {
        // Arrange
        ApiAuthCreate apiAuthCreate = new ApiAuthCreate();
        apiAuthCreate.setApiIds(java.util.Arrays.asList("api1", "api2"));

        HttpHeaders mockHeaders = new HttpHeaders();
        mockHeaders.add("x-auth-token", "test-token");

        when(apigClient.bindCredentials(any(), anyString(), anyString(), any(ApiAuthCreate.class))).thenThrow(
            new AgentStudioException("Connection refused"));

        // Act & Assert
        assertThrows(AgentStudioException.class, () -> {
            apigApiService.apiBindCredentials(apiAuthCreate);
        });
    }
}
