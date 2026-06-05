/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.validate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.LicenseAttrCode;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.service.config.KnowledgeBaseLicenseService;
import com.openjiuwen.studio.agent.agentbase.service.config.KnowledgeBaseSystemConfigService;
import com.openjiuwen.studio.agent.agentbase.service.resource.ResourceUsageFactory;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
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
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceValidateServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseSystemConfigService configService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseLicenseService licenseService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResourceUsageFactory resourceUsageFactory;

    @InjectMocks
    private ResourceValidateService resourceValidateService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(resourceValidateService, "configService", configService);
        ReflectionTestUtils.setField(resourceValidateService, "licenseService", licenseService);
        ReflectionTestUtils.setField(resourceValidateService, "resourceUsageFactory", resourceUsageFactory);
        ReflectionTestUtils.setField(resourceValidateService, "envType", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_resourceQuotaCheck_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR.getCode()), anyString()))
                    .thenReturn("not_empty");

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("not_empty");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(0L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(0L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCount(anyString())).thenReturn(0L);

                // When
                resourceValidateService.resourceQuotaCheck("not_empty", ResourceType.KNOWLEDGE_BASE, 0L);
            }
        });
    }

    @Test
    void test_resourceQuotaCheck_should_not_throw_exception3() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR.getCode()), anyString()))
                    .thenReturn(null);

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("mock_domain_id");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(100L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(100L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCount(anyString())).thenReturn(10L);

                // When
                resourceValidateService.resourceQuotaCheck("mock_domain_id", ResourceType.KNOWLEDGE_BASE, 1L);
            }
        });
    }

    @Test
    void test_resourceQuotaCheck_throw_exception() throws Exception {
        assertThrows(AgentBaseException.class, () -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR.getCode()), anyString()))
                    .thenReturn("not_empty");

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("not_empty");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(0L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(5L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(10L);
                ReflectionTestUtils.setField(resourceValidateService, "envType", "HC");
                // When
                resourceValidateService.resourceQuotaCheck("not_empty", ResourceType.KNOWLEDGE_BASE, 1L);
            }
        });
    }

    @Test
    void test_resourceQuotaCheckInKnowledgeBase_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR.getCode()), anyString()))
                    .thenReturn("resource capacity limit");

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("mock_domain_id");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(1L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(0L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(0L);

                // When
                resourceValidateService.resourceQuotaCheckInKnowledgeBase("mock_domain_id", ResourceType.KNOWLEDGE_BASE,
                    "mock_knowledge_id", 0L);
            }
        });
    }

    @Test
    void test_resourceQuotaCheckInKnowledgeBase_throw_exception() throws Exception {
        assertThrows(AgentBaseException.class, () -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR.getCode()), anyString()))
                    .thenReturn("resource capacity limit");

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("mock_domain_id");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(1L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(0L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(5L);

                // When
                resourceValidateService.resourceQuotaCheckInKnowledgeBase("mock_domain_id", ResourceType.KNOWLEDGE_BASE,
                    "mock_knowledge_id", 0L);
            }
        });
    }

    @Test
    void test_validateUploadFile_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                    RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("not_empty");
                mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("C");

                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString(), any(Object[].class)))
                    .thenReturn("not_empty");
                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn("not_empty");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(1L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(1L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(1L);
                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCount(anyString())).thenReturn(1L);
                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .knowledgeBaseTenantType("C")
                    .build();
                MultipartFile file = mock(MultipartFile.class, Answers.RETURNS_DEEP_STUBS);
                when(file.getSize()).thenReturn(0L);
                String[] supportedFileTypes = new String[0];
                // When
                resourceValidateService.validateUploadFile("not_empty", knowledgeBaseEntity, file, supportedFileTypes);
            }
        });
    }

    @Test
    void test_validateUploadFile_should_not_throw_exception_2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                    RETURNS_DEEP_STUBS);) {
                // Given
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("not_empty");
                mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("B");

                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString(), any(Object[].class)))
                    .thenReturn("not_empty");
                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn("not_empty");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(1L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(1L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(1L);
                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCount(anyString())).thenReturn(1L);
                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .knowledgeBaseTenantType("C")
                    .build();
                MultipartFile file = mock(MultipartFile.class, Answers.RETURNS_DEEP_STUBS);
                when(file.getSize()).thenReturn(0L);
                String[] supportedFileTypes = new String[0];
                // When
                resourceValidateService.validateUploadFile("not_empty", knowledgeBaseEntity, file, supportedFileTypes);
            }
        });
    }

    @Test
    void test_validateUploadFileStream_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<TenantTypeUtil> mockedStaticTenantTypeUtil = mockStatic(TenantTypeUtil.class,
                    RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString(), any(Object[].class)))
                    .thenReturn("not_empty");
                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn("not_empty");

                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                    .thenReturn("not_empty");
                mockedStaticTenantTypeUtil.when(TenantTypeUtil::getTenantType).thenReturn("B");

                when(configService.getSystemConfigValue(anyString(), anyString())).thenReturn(1L);

                when(licenseService.getLimitValue(anyString(), any(LicenseAttrCode.class))).thenReturn(1L);

                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCountInKnowledgeBase(anyString(), anyString())).thenReturn(1L);
                when(resourceUsageFactory.chooseResourceUsageQueryServiceMap(any(ResourceType.class))
                    .queryResourceUsageCount(anyString())).thenReturn(1L);

                Resource resource = mock(Resource.class, Answers.RETURNS_DEEP_STUBS);
                when(resource.contentLength()).thenReturn(0L);
                when(resource.getFilename()).thenReturn("not_empty");

                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .knowledgeBaseTenantType("C")
                    .build();

                String[] supportedFileTypes = new String[0];

                // When
                resourceValidateService.validateUploadFileStream(resource, "not_empty", knowledgeBaseEntity,
                    supportedFileTypes);
            }
        });
    }
}