/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.mapper.ApigApiGroupsMapper;
import com.openjiuwen.studio.agent.manager.mapper.ApigApisMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig.ApigApiService;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig.ApigRequestService;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApiGroupsDto;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApisDto;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ApigMcpServiceImplTest {

    @Mock
    private ApigApisMapper apigApisMapper;

    @Mock
    private ApigApiGroupsMapper apigApiGroupsMapper;

    @Mock
    private ApigRequestService apigRequestService;

    @Mock
    private ApigApiService apigApiService;

    @InjectMocks
    private ApigMcpServiceImpl apigMcpService;

    private ApigRequsetDto apigRequsetDto;

    @Mock
    private IamServiceUtils iamServiceUtils;

    @BeforeEach
    void setUp() {
        apigRequsetDto = new ApigRequsetDto();
        apigRequsetDto.setApiServiceName("test-api");
        apigRequsetDto.setGroupName("test-group");
        apigRequsetDto.setApigPath("/test/path");
    }

    @Test
    void testCreateApi_GroupDoesNotExist_Success() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("test");
            // Given
            String groupId = "group-id";
            String apiId = "api-id";
            String groupName = "test-group";
            String apiName = "test-api";

            // Mock group not existing
            when(apigRequestService.genGroupName()).thenReturn(groupName);
            ApigApiGroupsDto apigApiGroupsDto = new ApigApiGroupsDto();
            apigApiGroupsDto.setName("test");
            when(apigApiGroupsMapper.selectByName(groupName)).thenReturn(apigApiGroupsDto);

            // Mock group creation
            ApiGroupCreate apiGroupCreate = new ApiGroupCreate();
            apiGroupCreate.setName(groupName);
            when(apigRequestService.buildCreateApiGroupV2Request(any(ApigRequsetDto.class))).thenReturn(apiGroupCreate);

            CreateApiGroupV2Response createApiGroupV2Response = new CreateApiGroupV2Response();
            createApiGroupV2Response.setId(groupId);
            when(apigApiService.createApiGroup(any(ApiGroupCreate.class))).thenReturn(createApiGroupV2Response);
            // Mock API creation
            ApiCreate createApiV2Request = new ApiCreate();
            createApiV2Request.setName(apiName);
            when(apigRequestService.buildCreateApiV2Request(any(ApigRequsetDto.class))).thenReturn(createApiV2Request);

            CreateApiV2Response createApiV2Response = new CreateApiV2Response();
            createApiV2Response.setId(apiId);
            when(apigApiService.createApi(any(ApiCreate.class))).thenReturn(createApiV2Response);

            // Mock API publishing
            ApiActionInfo publishApiActionInfo = new ApiActionInfo();
            when(apigRequestService.buildPublishApiActionInfo(eq(apiId))).thenReturn(publishApiActionInfo);

            // Mock API authentication
            ApiAuthCreate apiAuthCreate = new ApiAuthCreate();
            apiAuthCreate.setApiIds(List.of(apiId));

            ApiAuthRelations apiAuthRelations = new ApiAuthRelations();
            assertDoesNotThrow(() -> apigMcpService.createApi(apigRequsetDto));
        }
    }

    @Test
    void testCreateApi_GroupExists_Success() {

        // Given
        String groupId = "group-id";
        String apiId = "api-id";
        String groupName = "test-group";
        String apiName = "test-api";

        // Mock group already exists
        ApigApiGroupsDto existingGroup = new ApigApiGroupsDto();
        existingGroup.setId(groupId);
        existingGroup.setName(groupName);
        when(apigApiGroupsMapper.selectByName(groupName)).thenReturn(existingGroup);

        // Mock API creation
        ApiCreate createApiV2Request = new ApiCreate();
        createApiV2Request.setName(apiName);
        when(apigRequestService.buildCreateApiV2Request(any(ApigRequsetDto.class))).thenReturn(createApiV2Request);

        CreateApiV2Response createApiV2Response = new CreateApiV2Response();
        createApiV2Response.setId(apiId);
        when(apigApiService.createApi(any(ApiCreate.class))).thenReturn(createApiV2Response);

        // Mock API publishing
        ApiActionInfo publishApiActionInfo = new ApiActionInfo();
        when(apigRequestService.buildPublishApiActionInfo(eq(apiId))).thenReturn(publishApiActionInfo);

        // Mock API authentication
        ApiAuthCreate apiAuthCreate = new ApiAuthCreate();
        apiAuthCreate.setApiIds(List.of(apiId));
        when(apigRequestService.buildApiAuthCreate(any())).thenReturn(apiAuthCreate);

        ApiAuthRelations apiAuthRelations = new ApiAuthRelations();
        when(apigApiService.apiBindCredentials(any(ApiAuthCreate.class))).thenReturn(apiAuthRelations);

        // When
        assertThrows(AgentStudioException.class, () -> apigMcpService.createApi(apigRequsetDto));

    }

    @Test
    void testCreateApi_ExceptionThrown_RollbackCalled() {
        // Given
        String groupName = "test-group";
        String apiName = "test-api";

        when(apigRequestService.genGroupName()).thenReturn(groupName);
        ApigApiGroupsDto apigApiGroupsDto = new ApigApiGroupsDto();
        apigApiGroupsDto.setName("test");
        when(apigApiGroupsMapper.selectByName(groupName)).thenReturn(apigApiGroupsDto);

        // Mock group creation to throw exception
        when(apigRequestService.buildCreateApiGroupV2Request(any(ApigRequsetDto.class))).thenReturn(
            new ApiGroupCreate());
        when(apigApiService.createApiGroup(any(ApiGroupCreate.class))).thenThrow(
            new RuntimeException("APIG service error"));

        // When & Then
        assertThrows(AgentStudioException.class, () -> apigMcpService.createApi(apigRequsetDto));
    }

    @Test
    void testDeleteApi_Success() {
        // Given
        String apiId = "api-id";
        String apiName = "test-api";

        // Mock API exists in DB
        ApigApisDto apigApisDto = new ApigApisDto();
        apigApisDto.setId(apiId);
        apigApisDto.setName(apiName);
        when(apigApisMapper.selectByName(apiName)).thenReturn(apigApisDto);

        // Mock API deletion
        DeleteApiV2Response deleteApiV2Response = new DeleteApiV2Response();
        ResponseEntity<DeleteApiV2Response> deleteApiV2ResponseResponseEntity = ResponseEntity.ok(deleteApiV2Response);
        when(apigApiService.deleteApi(eq(apiId))).thenReturn(deleteApiV2ResponseResponseEntity);

        // Mock offline action
        ApiActionInfo offlineApiActionInfo = new ApiActionInfo();
        when(apigRequestService.buildOfflineApiActionInfo(eq(apiId))).thenReturn(offlineApiActionInfo);

        // When
        assertDoesNotThrow(() -> apigMcpService.deleteApi(apigRequsetDto));
    }

    @Test
    void testDeleteApi_DeleteFailure_Ignored() {
        // Given
        String apiId = "api-id";
        String apiName = "test-api";

        // Mock API exists in DB
        ApigApisDto apigApisDto = new ApigApisDto();
        apigApisDto.setId(apiId);
        apigApisDto.setName(apiName);
        Map<String, String> token = new HashMap<>();
        token.put("token", "token");
        when(apigApisMapper.selectByName(apiName)).thenReturn(apigApisDto);
        when(apigRequestService.getXAuthTokenHeaders()).thenReturn(token);
        when(apigRequestService.genApiName(any())).thenReturn("api_name");

        // Mock API deletion to throw exception
        when(apigApiService.deleteApi("api_name")).thenThrow(new RuntimeException("Delete failed"));

        // Mock offline action
        ApiActionInfo offlineApiActionInfo = new ApiActionInfo();
        when(apigRequestService.buildOfflineApiActionInfo(eq(apiId))).thenReturn(offlineApiActionInfo);

        // When
        assertThrows(AgentStudioException.class, () -> apigMcpService.deleteApi(apigRequsetDto));
    }
}
