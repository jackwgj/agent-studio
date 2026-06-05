/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.config.ApigConfiguration;
import com.openjiuwen.studio.agent.manager.dto.McpServiceDetailInfoDto;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;
import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ApigRequestServiceTest {

    @InjectMocks
    private ApigRequestService apigRequestService;

    @Mock
    private IamServiceUtils iamServiceUtils;

    @Mock
    private ApigConfiguration apigConfiguration;

    @Test
    void testBuildCreateApiV2Request() {
        // Arrange
        ApigRequsetDto dto = new ApigRequsetDto();
        dto.setFuncUrn("test-func-urn");
        dto.setApigPath("/test/path");
        dto.setGroupId("test-group-id");
        dto.setApiServiceName("test-api-name");

        // Act
        ApiCreate result = apigRequestService.buildCreateApiV2Request(dto);

        // Assert
        assertNotNull(result);
        assertEquals("test-api-name", result.getName());
        assertEquals("latest", result.getVersion());
        assertEquals(ApiCreate.TypeEnum.NUMBER_1, result.getType());
        assertEquals(ApiCreate.ReqMethodEnum.ANY, result.getReqMethod());
        assertEquals(ApiCreate.ReqProtocolEnum.BOTH, result.getReqProtocol());
        assertEquals("/test/path", result.getReqUri());
        assertEquals(ApiCreate.AuthTypeEnum.APP, result.getAuthType());
        assertEquals(ApiCreate.MatchModeEnum.SWA, result.getMatchMode());
        assertEquals(ApiCreate.BackendTypeEnum.FUNCTION, result.getBackendType());
        assertEquals("test-group-id", result.getGroupId());
        assertNotNull(result.getFuncInfo());
    }

    @Test
    void testBuildCreateApiGroupV2Request() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);) {

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("id");
            // Arrange
            ApigRequsetDto dto = new ApigRequsetDto();
            dto.setApiServiceName("test-api-name");
            // Act
            ApiGroupCreate result = apigRequestService.buildCreateApiGroupV2Request(dto);

            // Assert
            assertNotNull(result);
            assertEquals("AgentBuilder_api_group_id", result.getName());
        }
    }

    @Test
    void testBuildPublishApiActionInfo() {
        // Arrange
        String apiId = "test-api-id";

        // Act
        ApiActionInfo result = apigRequestService.buildPublishApiActionInfo(apiId);

        // Assert
        assertNotNull(result);
        assertEquals("test-api-id", result.getApiId());
        assertEquals(ApiActionInfo.ActionEnum.ONLINE, result.getAction());
        assertEquals("DEFAULT_ENVIRONMENT_RELEASE_ID", result.getEnvId());
    }

    @Test
    void testBuildOfflineApiActionInfo() {
        // Arrange
        String apiId = "test-api-id";

        // Act
        ApiActionInfo result = apigRequestService.buildOfflineApiActionInfo(apiId);

        // Assert
        assertNotNull(result);
        assertEquals("test-api-id", result.getApiId());
        assertEquals(ApiActionInfo.ActionEnum.OFFLINE, result.getAction());
        assertEquals("DEFAULT_ENVIRONMENT_RELEASE_ID", result.getEnvId());
    }

    @Test
    void testBuildApiAuthCreate() {
        // Arrange
        List<String> apiIds = new ArrayList<>();
        apiIds.add("api1");
        apiIds.add("api2");

        // Act
        when(apigConfiguration.getEnvId()).thenReturn("test-env-id");
        ApiAuthCreate result = apigRequestService.buildApiAuthCreate(apiIds);
        // Assert
        assertNotNull(result);
        assertEquals("test-env-id", result.getEnvId());
        assertEquals(apiIds, result.getApiIds());
        assertEquals(1, result.getAppIds().size());
    }

    @Test
    void testBuildRemoveThrottleApiBindingCreate() {
        // Arrange
        String throttleBindingId = "test-throttle-binding-id";

        // Act
        String result = apigRequestService.buildRemoveThrottleApiBindingCreate(throttleBindingId);

        // Assert
        assertEquals("test-throttle-binding-id", result);
    }

    @Test
    void testGenGroupName() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("id");

            // Act
            String result = apigRequestService.genGroupName();

            // Assert
            assertEquals("AgentBuilder_api_group_id", result);
        }
    }

    @Test
    void testGenApiPathInApic() {
        // Arrange
        String serviceId = "test-service-id";

        // Act
        String result = apigRequestService.genApiPathInApic(serviceId);

        // Assert
        assertEquals("/mcp/test-service-id", result);
    }

    @Test
    void testGenApiName() {
        // Arrange
        ApigRequsetDto dto = new ApigRequsetDto();
        dto.setApiServiceName("test-api-name");

        // Act
        String result = apigRequestService.genApiName(dto);

        // Assert
        assertEquals("test-api-name", result);
    }

    @Test
    void testGetXAuthTokenHeaders() {
        // Arrange
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("test-auth-token");

        // Act
        Map<String, String> result = apigRequestService.getXAuthTokenHeaders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-auth-token", result.get("x-auth-token"));
    }

    @Test
    void testGetXAuthTokenHeaders2() {
        // Arrange
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("test-auth-token");

        // Act
        var result = apigRequestService.getXAuthTokenHeaders2();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("x-auth-token"));
        assertEquals("test-auth-token", result.getFirst("x-auth-token"));
    }

    @Test
    void testModifyRouterVo() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);) {

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId())
                .thenReturn("not_empty");
            // Arrange
            ApigRequsetDto dto = new ApigRequsetDto();
            dto.setServiceId("test-service-id");
            dto.setApiType("test-api-type");

            // Act
            apigRequestService.modifyRouterVo(dto);

            // Assert
            assertEquals("AgentBuilder_api_group_not_empty", dto.getGroupName());
            assertEquals("/mcp/test-service-id", dto.getApigPath());
            assertEquals("test-api-type", dto.getApiType());
        }
    }

    @Test
    public void test_buildApiAuthCreate() throws Exception {
        // Given
        List<String> apiIds = Arrays.asList("api1", "api2");
        String envId = "testEnvId";
        String credentialsId = "testCredentialsId";

        when(apigConfiguration.getEnvId()).thenReturn(envId);
        when(apigConfiguration.getCredentialsId()).thenReturn(credentialsId);

        // When
        ApiAuthCreate result = apigRequestService.buildApiAuthCreate(apiIds);

        // Then
        assertNotNull(result);
        assertEquals(envId, result.getEnvId());
        assertEquals(apiIds, result.getApiIds());
        assertEquals(Arrays.asList(credentialsId), result.getAppIds());
    }

    @Test
    void test_buildApiAuthCreate_should_return_not_null() throws Exception {
        // Given
        when(apigConfiguration.getCredentialsId()).thenReturn("not_empty");
        when(apigConfiguration.getEnvId()).thenReturn("not_empty");

        List<String> apiIds = new ArrayList<>();
        apiIds.add("not_empty");

        // When
        ApiAuthCreate result = apigRequestService.buildApiAuthCreate(apiIds);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_buildApiAuthCreate_should_return_not_null1() throws Exception {
        // Given
        when(apigConfiguration.getCredentialsId()).thenReturn(null);
        when(apigConfiguration.getEnvId()).thenReturn(null);

        // When
        ApiAuthCreate result = apigRequestService.buildApiAuthCreate(null);

        // Then
        assertNotNull(result);
    }
}
