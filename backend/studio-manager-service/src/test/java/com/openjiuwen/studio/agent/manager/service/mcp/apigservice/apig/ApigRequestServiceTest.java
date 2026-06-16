/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.config.ApigConfiguration;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;
import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApigRequestServiceTest {

    @Mock
    private IamServiceUtils iamServiceUtils;

    @Mock
    private ApigConfiguration apigConfiguration;

    @InjectMocks
    private ApigRequestService apigRequestService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testBuildCreateApiV2Request() {
        ApigRequsetDto dto = ApigRequsetDto.builder()
            .apiServiceName("test-api")
            .apigPath("/mcp/service-1")
            .groupId("group-1")
            .funcUrn("urn:func-1")
            .build();

        ApiCreate result = apigRequestService.buildCreateApiV2Request(dto);

        assertNotNull(result);
        assertEquals("test-api", result.getName());
        assertEquals("/mcp/service-1", result.getReqUri());
        assertEquals("group-1", result.getGroupId());
        assertNotNull(result.getFuncInfo());
        assertNotNull(result.getBackendApi());
    }

    @Test
    void testBuildCreateApiGroupV2Request() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            ApiGroupCreate result = apigRequestService.buildCreateApiGroupV2Request(ApigRequsetDto.builder().build());

            assertNotNull(result);
            assertEquals("AgentBuilder_api_group_domain-1", result.getName());
        }
    }

    @Test
    void testBuildPublishApiActionInfo() {
        ApiActionInfo result = apigRequestService.buildPublishApiActionInfo("api-1");

        assertNotNull(result);
        assertEquals("api-1", result.getApiId());
        assertEquals(ApiActionInfo.ActionEnum.ONLINE, result.getAction());
        assertEquals("DEFAULT_ENVIRONMENT_RELEASE_ID", result.getEnvId());
    }

    @Test
    void testBuildOfflineApiActionInfo() {
        ApiActionInfo result = apigRequestService.buildOfflineApiActionInfo("api-1");

        assertNotNull(result);
        assertEquals("api-1", result.getApiId());
        assertEquals(ApiActionInfo.ActionEnum.OFFLINE, result.getAction());
        assertEquals("DEFAULT_ENVIRONMENT_RELEASE_ID", result.getEnvId());
    }

    @Test
    void testBuildApiAuthCreate() {
        when(apigConfiguration.getEnvId()).thenReturn("env-id-1");
        when(apigConfiguration.getCredentialsId()).thenReturn("cred-id-1");

        List<String> apiIds = List.of("api-1", "api-2");

        ApiAuthCreate result = apigRequestService.buildApiAuthCreate(apiIds);

        assertNotNull(result);
        assertEquals("env-id-1", result.getEnvId());
        assertEquals(apiIds, result.getApiIds());
        assertEquals(1, result.getAppIds().size());
        assertEquals("cred-id-1", result.getAppIds().get(0));
    }

    @Test
    void testBuildRemoveThrottleApiBindingCreate() {
        String result = apigRequestService.buildRemoveThrottleApiBindingCreate("throttle-1");

        assertEquals("throttle-1", result);
    }

    @Test
    void testGenGroupName() {
        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            String result = apigRequestService.genGroupName();

            assertEquals("AgentBuilder_api_group_domain-1", result);
        }
    }

    @Test
    void testGenApiPathInApic() {
        String result = apigRequestService.genApiPathInApic("service-1");

        assertEquals("/mcp/service-1", result);
    }

    @Test
    void testGenApiName() {
        ApigRequsetDto dto = ApigRequsetDto.builder()
            .apiServiceName("my-api-service")
            .build();

        String result = apigRequestService.genApiName(dto);

        assertEquals("my-api-service", result);
    }

    @Test
    void testGetXAuthTokenHeaders() {
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("token-123");

        Map<String, String> result = apigRequestService.getXAuthTokenHeaders();

        assertNotNull(result);
        assertEquals("token-123", result.get("x-auth-token"));
    }

    @Test
    void testGetXAuthTokenHeaders2() {
        when(iamServiceUtils.getOpAccountAuthToken()).thenReturn("token-456");

        org.springframework.http.HttpHeaders result = apigRequestService.getXAuthTokenHeaders2();

        assertNotNull(result);
        assertEquals(List.of("token-456"), result.get("x-auth-token"));
    }

    @Test
    void testModifyRouterVo() {
        ApigRequsetDto dto = ApigRequsetDto.builder()
            .serviceId("svc-1")
            .apiType("function")
            .build();

        try (MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            ctx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            apigRequestService.modifyRouterVo(dto);

            assertEquals("AgentBuilder_api_group_domain-1", dto.getGroupName());
            assertEquals("/mcp/svc-1", dto.getApigPath());
            assertEquals("function", dto.getApiType());
        }
    }
}
