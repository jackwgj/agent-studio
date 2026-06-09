/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.model.QueryResourceUsageResponse;
import com.openjiuwen.studio.agent.agentbase.service.resource.ResourceUsageFactory;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;

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
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeBaseResourceManagementServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResourceUsageFactory resourceUsageFactory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResourceValidateService resourceValidateService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @InjectMocks
    private KnowledgeBaseResourceManagementService
        knowledgeBaseResourceManagementService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(knowledgeBaseResourceManagementService, "resourceUsageFactory",
            resourceUsageFactory);
        ReflectionTestUtils.setField(knowledgeBaseResourceManagementService, "resourceValidateService",
            resourceValidateService);
        ReflectionTestUtils.setField(knowledgeBaseResourceManagementService, "knowledgeBaseMapper",
            knowledgeBaseMapper);
        ReflectionTestUtils.setField(knowledgeBaseResourceManagementService, "envType", "hc");
        ReflectionTestUtils.setField(knowledgeBaseResourceManagementService, "enterpriseKnowledgeBaseLimit", -1L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_queryResourceUsage_should_return_not_null() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain_id");

            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                .thenReturn("not_empty");
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.INVALID_PARAMETER.getCode()), eq("knowledge_base_id")))
                .thenReturn("knowledge_base_id");

            mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("B");

            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(50L);
            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCount(anyString())).thenReturn(5L);

            KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder().build();
            when(knowledgeBaseMapper.selectById(anyString(), anyString())).thenReturn(knowledgeBaseEntity);

            when(resourceValidateService.queryQuotaLimit(any())).thenReturn(100L);

            // When
            QueryResourceUsageResponse result = knowledgeBaseResourceManagementService.queryResourceUsage("not_empty",
                "knowledgeBase", "queryResourceUsageQo");

            QueryResourceUsageResponse result2 = knowledgeBaseResourceManagementService.queryResourceUsage("not_empty",
                "knowledgeBaseDateset", "queryResourceUsageQo");

            assertNotNull(result);
            assertNotNull(result2);
        }
    }

    @Test
    void test_queryResourceUsage_should_not_throw_exception() {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);

                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                    .thenReturn(null);
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.INVALID_PARAMETER.getCode()), eq("knowledge_base_id")))
                    .thenReturn(null);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(null);
                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                    .queryResourceUsageCount(anyString())).thenReturn(null);
                KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
                when(knowledgeBaseMapper.selectById(anyString(), anyString())).thenReturn(knowledgeBaseEntity);
                knowledgeBaseEntity.setKnowledgeBaseTenantType("C");
                when(resourceValidateService.queryQuotaLimit(any())).thenReturn(null);

                // When
                QueryResourceUsageResponse result = knowledgeBaseResourceManagementService.queryResourceUsage(
                    "not_empty", "knowledgeBaseDateset", "queryResourceUsageQo");

                assertNotNull(result);
            }
        });
    }

    @Test
    void test_queryResourceUsage_should_not_throw_exception_2() {

        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);
            mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("C");

            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                .thenReturn(null);
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.INVALID_PARAMETER.getCode()), eq("knowledge_base_id")))
                .thenReturn(null);

            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(null);
            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCount(anyString())).thenReturn(null);
            KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
            when(knowledgeBaseMapper.selectById(anyString(), anyString())).thenReturn(knowledgeBaseEntity);
            knowledgeBaseEntity.setKnowledgeBaseTenantType("C");
            when(resourceValidateService.queryQuotaLimit(any())).thenReturn(50L);

            QueryResourceUsageResponse result = knowledgeBaseResourceManagementService.queryResourceUsage("not_empty",
                "knowledgeBaseDatesetStorage", "queryResourceUsageQo");
            assertNotNull(result);
        }
    }

    @Test
    void test_queryResourceUsage_should_not_throw_exception_3() {

        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);
            mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("B");

            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                .thenReturn(null);
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.INVALID_PARAMETER.getCode()), eq("knowledge_base_id")))
                .thenReturn(null);

            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(null);
            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCount(anyString())).thenReturn(null);
            KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
            when(knowledgeBaseMapper.selectById(anyString(), anyString())).thenReturn(knowledgeBaseEntity);
            knowledgeBaseEntity.setKnowledgeBaseTenantType("C");
            when(resourceValidateService.queryQuotaLimit(any())).thenReturn(50L);

            QueryResourceUsageResponse result = knowledgeBaseResourceManagementService.queryResourceUsage("not_empty",
                "knowledgeBaseDatesetStorage", "queryResourceUsageQo");
            assertNotNull(result);
        }
    }

    @Test
    void test_queryResourceUsage_should_not_throw_exception_4() {

        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);
            mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("B");

            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                .thenReturn(null);
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.INVALID_PARAMETER.getCode()), eq("knowledge_base_id")))
                .thenReturn(null);

            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(null);
            when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any())
                .queryResourceUsageCount(anyString())).thenReturn(null);
            KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
            when(knowledgeBaseMapper.selectById(anyString(), anyString())).thenReturn(knowledgeBaseEntity);
            knowledgeBaseEntity.setKnowledgeBaseTenantType("B");
            when(resourceValidateService.queryQuotaLimit(any())).thenReturn(50L);

            QueryResourceUsageResponse result = knowledgeBaseResourceManagementService.queryResourceUsage("not_empty",
                "knowledgeBaseDatesetStorage", "queryResourceUsageQo");
            assertNotNull(result);
        }
    }
}