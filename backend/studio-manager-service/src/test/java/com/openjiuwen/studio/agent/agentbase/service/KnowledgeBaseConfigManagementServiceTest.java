/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.model.QueryResourceUsageResponse;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeConnectionRouterService;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBaseConfigsResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeBaseConfigManagementServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResourceValidateService validateService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MultipartProperties multipartProperties;

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock
    private KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    @Mock
    private com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseResourceManagementService
        resourceManagementService;

    @InjectMocks
    private KnowledgeBaseConfigManagementService knowledgeBaseConfigManagementService;

    private AutoCloseable mockitoCloseable;

    private static final String PROJECT_ID = "project_123";

    private static final String CONN_ID = "conn_789";

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(knowledgeBaseConfigManagementService, "validateService", validateService);
        ReflectionTestUtils.setField(knowledgeBaseConfigManagementService, "multipartProperties", multipartProperties);
        ReflectionTestUtils.setField(knowledgeBaseConfigManagementService, "knowledgeBaseMapper", knowledgeBaseMapper);
        ReflectionTestUtils.setField(knowledgeBaseConfigManagementService, "knowledgeBaseConnectionMapper",
            knowledgeBaseConnectionMapper);
        ReflectionTestUtils.setField(knowledgeBaseConfigManagementService, "resourceManagementService",
            resourceManagementService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_listKnowledgeBaseConfigs_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(TenantTypeUtil::getTenantType).thenReturn("C");

            when(multipartProperties.getMaxFileSize().toBytes()).thenReturn(0L);

            when(validateService.queryQuotaLimit(eq(ResourceType.SINGLE_FILE_SIZE_FREE_KNOWLEDGE_BASE))).thenReturn(0L);

            when(resourceManagementService.queryResourceUsage(anyString(), anyString(), any())).thenReturn(
                new QueryResourceUsageResponse().setMaxValue(-1L));

            // When
            ListKnowledgeBaseConfigsResponse result = knowledgeBaseConfigManagementService.listKnowledgeBaseConfigs(
                "not_empty", "not_empty", "not_empty");

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listKnowledgeBaseConfigs_should_not_throw_exception() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> TenantTypeUtil.getTenantType()).thenReturn("B");

            when(multipartProperties.getMaxFileSize().toBytes()).thenReturn(0L);

            when(validateService.queryQuotaLimit(eq(ResourceType.SINGLE_FILE_SIZE_FREE_KNOWLEDGE_BASE))).thenReturn(
                null);
            when(resourceManagementService.queryResourceUsage(anyString(), anyString(), any())).thenReturn(
                new QueryResourceUsageResponse().setMaxValue(-1L));

            // When
            ListKnowledgeBaseConfigsResponse result = knowledgeBaseConfigManagementService.listKnowledgeBaseConfigs(
                "not_empty", null, "not_empty");
        }
    }

    @Test
    void test_listKnowledgeBaseConfigs_should_return_successfully_1() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(TenantTypeUtil::getTenantType).thenReturn("C");

            // 1. 准备 KB 对象，确保 ConnectionId 必须有值
            KnowledgeBaseEntity kb = new KnowledgeBaseEntity();
            kb.setKnowledgeBaseConnectionId("REAL_CONN_ID_123");

            // 2. 准备 Connection 对象，确保包含正确的参数
            KnowledgeBaseConnectionEntity conn = new KnowledgeBaseConnectionEntity();
            KnowledgeBaseConnectionParam param = new KnowledgeBaseConnectionParam();
            param.setCode("KB_MODEL_MANAGE_URL");
            param.setValue("http://model-url.com");
            conn.setParams(Collections.singletonList(param));

            // 3. 关键：强制让所有 Mapper 只要被调用就返回预设对象
            when(knowledgeBaseMapper.selectById(any(), any())).thenReturn(kb);
            when(knowledgeBaseConnectionMapper.findById(any())).thenReturn(conn);
            when(resourceManagementService.queryResourceUsage(anyString(), anyString(), any())).thenReturn(
                new QueryResourceUsageResponse().setMaxValue(-1L));

            // When
            ListKnowledgeBaseConfigsResponse result = knowledgeBaseConfigManagementService.listKnowledgeBaseConfigs(
                "any_proj", "any_base_id", "any_kb");

            // Then
            assertNotNull(result);
            assertEquals("http://model-url.com", result.getConfigs().get(1).getValue());
        }
    }

}