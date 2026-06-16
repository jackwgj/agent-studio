/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.model.QueryResourceUsageResponse;
import com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseResourceManagementService;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ResourceUsageDetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ResourceManagementServiceTest {

    @Mock
    private SkuManageService skuManageService;

    @Mock
    private KnowledgeBaseResourceManagementService knowledgeBaseResourceManagementService;

    @InjectMocks
    private ResourceManagementService resourceManagementService;

    @Test
    void testResourceUsageDetails_BlankResourceType() {
        ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1", "");
        assertNotNull(result);
        assertEquals(0, result.getTotalAmount());
        assertEquals(0, result.getUsedLimit());
    }

    @Test
    void testResourceUsageDetails_NullResourceType() {
        ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1", null);
        assertNotNull(result);
        assertEquals(0, result.getTotalAmount());
    }

    @Test
    void testResourceUsageDetails_WorkflowType() {
        when(skuManageService.queryAppCountExceededLimit()).thenReturn(List.of(5, 10));

        ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1",
            Constants.AppType.WORKFLOW);
        assertNotNull(result);
        assertEquals(5, result.getUsedLimit());
        assertEquals(10, result.getTotalAmount());
        assertFalse(result.isIsExceedLimit());
    }

    @Test
    void testResourceUsageDetails_AgentType_ExceedLimit() {
        when(skuManageService.queryAppCountExceededLimit()).thenReturn(List.of(10, 10));

        ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1",
            Constants.AppType.AGENT);
        assertNotNull(result);
        assertTrue(result.isIsExceedLimit());
    }

    @Test
    void testResourceUsageDetails_AgentType_UnlimitedLimit() {
        when(skuManageService.queryAppCountExceededLimit()).thenReturn(List.of(5, -1));

        ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1",
            Constants.AppType.AGENT_NEW);
        assertNotNull(result);
        assertFalse(result.isIsExceedLimit());
    }

    @Test
    void testResourceUsageDetails_KnowledgeBaseType() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            QueryResourceUsageResponse response = new QueryResourceUsageResponse();
            response.setUsedValue(5L);
            response.setMaxValue(10L);
            when(knowledgeBaseResourceManagementService.queryResourceUsage(eq("proj-1"), anyString(), any()))
                .thenReturn(response);

            ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1",
                Constants.KnowledgeBase.KNOWLEDGE_BASE);
            assertNotNull(result);
            assertEquals(5, result.getUsedLimit());
            assertEquals(10, result.getTotalAmount());
        }
    }

    @Test
    void testResourceUsageDetails_UnknownType() {
        ResourceUsageDetails result = resourceManagementService.resourceUsageDetails("proj-1", "unknown-type");
        assertNotNull(result);
        assertEquals(0, result.getUsedLimit());
        assertEquals(0, result.getTotalAmount());
    }

    @Test
    void testQueryResourceUsage_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            QueryResourceUsageResponse response = new QueryResourceUsageResponse();
            response.setUsedValue(3L);
            response.setMaxValue(10L);
            when(knowledgeBaseResourceManagementService.queryResourceUsage(eq("proj-1"), eq("kb"), any()))
                .thenReturn(response);

            ResourceUsageDetails result = resourceManagementService.queryResourceUsage("kb");
            assertNotNull(result);
            assertEquals(3, result.getUsedLimit());
            assertEquals(10, result.getTotalAmount());
            assertFalse(result.isIsExceedLimit());
        }
    }

    @Test
    void testQueryResourceUsage_NullResponse() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            when(knowledgeBaseResourceManagementService.queryResourceUsage(anyString(), anyString(), any()))
                .thenReturn(null);

            ResourceUsageDetails result = resourceManagementService.queryResourceUsage("kb");
            assertNotNull(result);
        }
    }

    @Test
    void testQueryResourceUsage_UnlimitedResource() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            QueryResourceUsageResponse response = new QueryResourceUsageResponse();
            response.setUsedValue(5L);
            response.setMaxValue(Long.valueOf(CommonConstant.UNLIMITED_RESOURCE_VALUE));
            when(knowledgeBaseResourceManagementService.queryResourceUsage(eq("proj-1"), eq("kb"), any()))
                .thenReturn(response);

            ResourceUsageDetails result = resourceManagementService.queryResourceUsage("kb");
            assertEquals(-1, result.getTotalAmount());
            assertFalse(result.isIsExceedLimit());
        }
    }
}
