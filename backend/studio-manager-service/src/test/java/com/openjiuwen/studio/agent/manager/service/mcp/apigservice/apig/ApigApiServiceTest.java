package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig;

import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthRelations;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;
import com.huaweicloud.sdk.apig.v2.model.CreateApiGroupV2Response;
import com.huaweicloud.sdk.apig.v2.model.CreateApiV2Response;
import com.huaweicloud.sdk.apig.v2.model.DeleteApiV2Response;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.rce.client.ApigClient;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigGroupResp;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApigApiServiceTest {

    @Mock
    private ApigClient apigClient;

    @Mock
    private IamServiceUtils iamServiceUtils;

    @InjectMocks
    private ApigApiService apigApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apigApiService, "projectId", "test-project-id");
        ReflectionTestUtils.setField(apigApiService, "instanceId", "test-instance-id");
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("test-token");
    }

    @Test
    void testCreateApi_Success() {
        CreateApiV2Response expectedResponse = mock(CreateApiV2Response.class);
        when(apigClient.createApi(eq("test-token"), eq("test-project-id"), eq("test-instance-id"), any(ApiCreate.class)))
            .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        ApiCreate request = mock(ApiCreate.class);
        CreateApiV2Response result = apigApiService.createApi(request);

        assertEquals(expectedResponse, result);
    }

    @Test
    void testCreateApi_Exception() {
        when(apigClient.createApi(anyString(), anyString(), anyString(), any(ApiCreate.class)))
            .thenThrow(new RuntimeException("api error"));

        ApiCreate request = mock(ApiCreate.class);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.createApi(request));
        assertEquals(StudioError.NO_CREATOR_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testDeleteApi_Success() {
        DeleteApiV2Response expectedResponse = mock(DeleteApiV2Response.class);
        ResponseEntity<DeleteApiV2Response> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(apigClient.deleteApi(eq("test-token"), eq("test-project-id"), eq("test-instance-id"), eq("api-1")))
            .thenReturn(responseEntity);

        ResponseEntity<DeleteApiV2Response> result = apigApiService.deleteApi("api-1");

        assertEquals(responseEntity, result);
    }

    @Test
    void testDeleteApi_Exception() {
        when(apigClient.deleteApi(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("delete error"));

        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.deleteApi("api-1"));
        assertEquals(StudioError.API_DELETE_API_ERROR, ex.getErrorCode());
    }

    @Test
    void testCreateApiGroup_Success() {
        CreateApiGroupV2Response expectedResponse = mock(CreateApiGroupV2Response.class);
        when(apigClient.createApiGroup(eq("test-token"), eq("test-project-id"), eq("test-instance-id"), any(ApiGroupCreate.class)))
            .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        ApiGroupCreate request = mock(ApiGroupCreate.class);
        CreateApiGroupV2Response result = apigApiService.createApiGroup(request);

        assertEquals(expectedResponse, result);
    }

    @Test
    void testCreateApiGroup_Exception() {
        when(apigClient.createApiGroup(anyString(), anyString(), anyString(), any(ApiGroupCreate.class)))
            .thenThrow(new RuntimeException("group error"));

        ApiGroupCreate request = mock(ApiGroupCreate.class);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.createApiGroup(request));
        assertEquals(StudioError.API_KEY_CREATE_ERROR, ex.getErrorCode());
    }

    @Test
    void testListApiGroup_Success() {
        ApigGroupResp expectedResp = mock(ApigGroupResp.class);
        when(apigClient.listApiGroups(eq("test-token"), eq("test-project-id"), eq("test-instance-id"), eq("group-name")))
            .thenReturn(new ResponseEntity<>(expectedResp, HttpStatus.OK));

        ApigGroupResp result = apigApiService.listApiGroup("group-name");

        assertEquals(expectedResp, result);
    }

    @Test
    void testListApiGroup_Exception_ReturnsNull() {
        when(apigClient.listApiGroups(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("list error"));

        ApigGroupResp result = apigApiService.listApiGroup("group-name");

        assertNull(result);
    }

    @Test
    void testPublishOrOfflineApi_Success() {
        when(apigClient.publishOrOfflineApi(eq("test-token"), eq("test-project-id"), eq("test-instance-id"), any(ApiActionInfo.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ApiActionInfo request = mock(ApiActionInfo.class);
        apigApiService.publishOrOfflineApi(request);

        verify(apigClient).publishOrOfflineApi("test-token", "test-project-id", "test-instance-id", request);
    }

    @Test
    void testPublishOrOfflineApi_Exception() {
        when(apigClient.publishOrOfflineApi(anyString(), anyString(), anyString(), any(ApiActionInfo.class)))
            .thenThrow(new RuntimeException("publish error"));

        ApiActionInfo request = mock(ApiActionInfo.class);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.publishOrOfflineApi(request));
        assertEquals(StudioError.API_DELETE_API_ERROR, ex.getErrorCode());
    }

    @Test
    void testApiBindCredentials_Success() {
        String jsonBody = "{\"authRelations\": []}";
        ResponseEntity<String> response = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(apigClient.bindCredentials(eq("test-token"), eq("test-project-id"), eq("test-instance-id"), any(ApiAuthCreate.class)))
            .thenReturn(response);

        ApiAuthRelations expectedRelations = mock(ApiAuthRelations.class);
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            jsonUtilsMock.when(() -> JsonUtils.toObject(jsonBody, ApiAuthRelations.class)).thenReturn(expectedRelations);

            ApiAuthCreate apiAuthCreate = mock(ApiAuthCreate.class);
            ApiAuthRelations result = apigApiService.apiBindCredentials(apiAuthCreate);

            assertEquals(expectedRelations, result);
        }
    }

    @Test
    void testApiBindCredentials_ErrorResponse_APIG2015() {
        String errorBody = "{\"error_code\": \"APIG.2015\", \"error_msg\": \"IP error\"}";
        ResponseEntity<String> response = new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
        when(apigClient.bindCredentials(anyString(), anyString(), anyString(), any(ApiAuthCreate.class)))
            .thenReturn(response);

        ApiAuthCreate apiAuthCreate = mock(ApiAuthCreate.class);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.apiBindCredentials(apiAuthCreate));
        assertEquals(StudioError.INVALID_URL, ex.getErrorCode());
    }

    @Test
    void testApiBindCredentials_ErrorResponse_OtherErrorCode() {
        String errorBody = "{\"error_code\": \"APIG.1001\", \"error_msg\": \"other error\"}";
        ResponseEntity<String> response = new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
        when(apigClient.bindCredentials(anyString(), anyString(), anyString(), any(ApiAuthCreate.class)))
            .thenReturn(response);

        ApiAuthCreate apiAuthCreate = mock(ApiAuthCreate.class);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.apiBindCredentials(apiAuthCreate));
        assertEquals(StudioError.INVALID_URL, ex.getErrorCode());
    }

    @Test
    void testApiBindCredentials_ErrorResponse_InvalidJson() {
        String errorBody = "not valid json";
        ResponseEntity<String> response = new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(apigClient.bindCredentials(anyString(), anyString(), anyString(), any(ApiAuthCreate.class)))
            .thenReturn(response);

        ApiAuthCreate apiAuthCreate = mock(ApiAuthCreate.class);
        AgentStudioException ex = assertThrows(AgentStudioException.class, () -> apigApiService.apiBindCredentials(apiAuthCreate));
        assertEquals(StudioError.INVALID_URL, ex.getErrorCode());
    }
}
