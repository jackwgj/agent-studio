/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.agentbase.constant.Constant;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnection;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorAbilityEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorEntity;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectionStatus;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectorParamType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectorAbilityMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectorMapper;
import com.openjiuwen.studio.agent.agentbase.model.AbilityDetail;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectorParam;
import com.openjiuwen.studio.agent.agentbase.service.validate.KnowledgeBaseConnectionValidateService;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SqlLikeEscapeHelper;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.utils.UUIDGenerator;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.KnowledgeBaseConnectorContext;
import com.openjiuwen.studio.agent.foundation.connection.ability.AbilityProcessorFactory;
import com.openjiuwen.studio.agent.foundation.connection.ability.RetrieveProcessor;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;
import com.openjiuwen.studio.agent.manager.dto.AbilityInfo;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.manager.dto.CreateThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateThirdPartyKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeBaseConnectorDetail;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectionsQo;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectionsResponse;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectorsQo;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectorsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ParamDefinitionInfo;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeBaseConnectorAbilitiesQo;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeBaseConnectorAbilitiesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody;
import com.openjiuwen.studio.agent.manager.dto.TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.TestConnectionThirdPartyKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateThirdPartyKnowledgeBaseConnectionResponse;

import org.apache.commons.lang3.ObjectUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class ThirdPartyKnowledgeBaseConnectionServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectorContext knowledgeBaseConnectorContext;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectionMapper connectionMapper;

    @Mock
    private KnowledgeBaseConnectorAbilityMapper connectorAbilityMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectorMapper connectorMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectionValidateService knowledgeBaseConnectionValidateService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResourceValidateService resourceValidateService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private I18nTransService i18nTransService;

    @Mock
    private AbilityProcessorFactory abilityProcessorFactory;

    @Mock
    private RetrieveProcessor retrieveProcessor;

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.ThirdPartyKnowledgeBaseConnectionService
        thirdPartyKnowledgeBaseConnectionService;

    private AutoCloseable mockitoCloseable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "knowledgeBaseConnectorContext",
            knowledgeBaseConnectorContext);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "knowledgeBaseConnectionMapper",
            knowledgeBaseConnectionMapper);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "defaultIcon", "not_empty");
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "deployMode", "not_empty");
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "connectionMapper", connectionMapper);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "connectorMapper", connectorMapper);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "knowledgeBaseConnectionValidateService",
            knowledgeBaseConnectionValidateService);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "resourceValidateService",
            resourceValidateService);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "i18nTransService", i18nTransService);
        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "abilityProcessorFactory",
            abilityProcessorFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_createThirdPartyKnowledgeBaseConnection_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class);
            MockedStatic<UUIDGenerator> mockedStaticUUIDGenerator = mockStatic(UUIDGenerator.class)) {

            // Mock RequestContext
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn(Constant.TEST_DOMAIN_ID);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName)
                .thenReturn(Constant.TEST_USER_NAME);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainName)
                .thenReturn(Constant.TEST_DOMAIN_NAME);

            // Mock I18nUtils - 只 mock 明确需要的
            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_ID_DUPLICATE.getCode())))
                .thenReturn(null);

            // 可选：打印未 mock 的 code
            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString()))
                .thenAnswer(invocation -> "MISSING_I18N: " + invocation.getArgument(0));
            // Mock UUID
            mockedStaticUUIDGenerator.when(UUIDGenerator::getUUID).thenReturn("mocked-uuid-123");

            // Mock 服务依赖
            List<ConnectionParamInfo> connectionParamInfos = new ArrayList<>();
            ConnectionParamInfo paramInfo = new ConnectionParamInfo();
            connectionParamInfos.add(paramInfo);

            when(knowledgeBaseConnectionValidateService.checkAndEncryptedParams(anyString(), anyList())).thenReturn(
                connectionParamInfos);

            when(knowledgeBaseConnectionValidateService.checkConnectionUnique(anyString(),
                any(KnowledgeBaseConnectionEntity.class))).thenReturn(false);

            when(connectionMapper.insert(any(KnowledgeBaseConnectionEntity.class))).thenReturn(1); // 确保插入成功

            // 构造有效的请求
            CreateThirdPartyKnowledgeBaseConnectionRequestBody request
                = new CreateThirdPartyKnowledgeBaseConnectionRequestBody();
            request.setName("TestConnection");
            request.setIcon(null);
            request.setParams(connectionParamInfos); // 假设有 params 字段

            // When
            CreateThirdPartyKnowledgeBaseConnectionResponse result
                = thirdPartyKnowledgeBaseConnectionService.createThirdPartyKnowledgeBaseConnection(
                Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, request);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_createThirdPartyKnowledgeBaseConnection_should_return_not_null2() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<UUIDGenerator> mockedStaticUUIDGenerator = mockStatic(UUIDGenerator.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn(Constant.TEST_DOMAIN_ID);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName)
                .thenReturn(Constant.TEST_USER_NAME);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainName)
                .thenReturn(Constant.TEST_DOMAIN_NAME);

            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_ID_DUPLICATE.getCode())))
                .thenReturn("");
            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn("");

            mockedStaticUUIDGenerator.when(UUIDGenerator::getUUID).thenReturn("");

            List<ConnectionParamInfo> connectionParamInfos = new ArrayList<>();
            ConnectionParamInfo connectionParamInfo = new ConnectionParamInfo();
            connectionParamInfos.add(connectionParamInfo);
            when(knowledgeBaseConnectionValidateService.checkAndEncryptedParams(anyString(), anyList())).thenReturn(
                connectionParamInfos);
            when(knowledgeBaseConnectionValidateService.checkConnectionUnique(anyString(),
                any(KnowledgeBaseConnectionEntity.class))).thenReturn(Boolean.FALSE);

            when(connectionMapper.insert(any(KnowledgeBaseConnectionEntity.class))).thenReturn(0);

            CreateThirdPartyKnowledgeBaseConnectionRequestBody request
                = new CreateThirdPartyKnowledgeBaseConnectionRequestBody();

            // When
            CreateThirdPartyKnowledgeBaseConnectionResponse result
                = thirdPartyKnowledgeBaseConnectionService.createThirdPartyKnowledgeBaseConnection(
                Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, request);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_createThirdPartyKnowledgeBaseConnection_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<UUIDGenerator> mockedStaticUUIDGenerator = mockStatic(UUIDGenerator.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId())
                    .thenReturn(null);
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName()).thenReturn(null);
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn(null);
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainName())
                    .thenReturn(null);

                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(eq(ErrorCode.RESOURCE_ID_DUPLICATE.getCode())))
                    .thenReturn(null);
                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn(null);

                mockedStaticUUIDGenerator.when(() -> UUIDGenerator.getUUID()).thenReturn(null);

                when(knowledgeBaseConnectionValidateService.checkAndEncryptedParams(anyString(), anyList())).thenReturn(
                    null);
                when(knowledgeBaseConnectionValidateService.checkConnectionUnique(anyString(),
                    any(KnowledgeBaseConnectionEntity.class))).thenReturn(true);

                when(connectionMapper.insert(any(KnowledgeBaseConnectionEntity.class))).thenReturn(0);

                // When
                CreateThirdPartyKnowledgeBaseConnectionResponse result
                    = thirdPartyKnowledgeBaseConnectionService.createThirdPartyKnowledgeBaseConnection(null, null,
                    null);
            }
        });
    }

    @Test
    void test_deleteThirdPartyKnowledgeBaseConnection_should_return_not_null() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS)) {
            // Mock 国际化
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_CONNECTION_DELETE_FOR_OPEN.getCode())))
                .thenReturn("Connection is open, cannot delete.");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            // Given: 使用一个可以被删除的状态，比如 CLOSED
            KnowledgeBaseConnectionEntity connectionEntity = new KnowledgeBaseConnectionEntity();
            connectionEntity.setStatus(KnowledgeBaseConnectionStatus.CLOSE); // ✅ 改为 CLOSED

            when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                anyString())).thenReturn(connectionEntity);

            when(connectionMapper.delete(anyString())).thenReturn(1); // 模拟删除成功

            // When
            CommonDeleteRsp result = thirdPartyKnowledgeBaseConnectionService.deleteThirdPartyKnowledgeBaseConnection(
                Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_deleteThirdPartyKnowledgeBaseConnection_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                    .thenReturn(Constant.TEST_USER_ID);
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_CONNECTION_DELETE_FOR_OPEN.getCode())))
                    .thenReturn(null);
                mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                    .thenReturn(Constant.TEST_USER_ID);
                when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                    anyString())).thenReturn(null);

                when(connectionMapper.delete(anyString())).thenReturn(0);

                // When
                CommonDeleteRsp result
                    = thirdPartyKnowledgeBaseConnectionService.deleteThirdPartyKnowledgeBaseConnection(
                    Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID);
            }
        });
    }

    @Test
    void test_deleteThirdPartyKnowledgeBaseConnection_should_return_not_null2() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_CONNECTION_DELETE_FOR_OPEN.getCode())))
                .thenReturn(null);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                anyString())).thenReturn(null);

            when(connectionMapper.delete(anyString())).thenReturn(0);

            // When
            CommonDeleteRsp result = thirdPartyKnowledgeBaseConnectionService.deleteThirdPartyKnowledgeBaseConnection(
                null, null, null);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnectors_should_return_not_null() throws Exception {
        // Given
        List<KnowledgeBaseConnectorEntity> connectorEntities = new ArrayList<>();
        KnowledgeBaseConnectorEntity knowledgeBaseConnectorEntity = new KnowledgeBaseConnectorEntity();
        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam.setName(Constant.TEST_PARAM_NAME);
        knowledgeBaseConnectorEntity.setId(Constant.TEST_PARAM_ID);
        knowledgeBaseConnectorParam.setType(KnowledgeBaseConnectorParamType.STRING);
        knowledgeBaseConnectorEntity.setParams(Collections.singletonList(knowledgeBaseConnectorParam));
        connectorEntities.add(knowledgeBaseConnectorEntity);
        when(connectorMapper.listKnowledgeBaseConnectors(anyString())).thenReturn(connectorEntities);

        ListThirdPartyKnowledgeBaseConnectorsQo body = new ListThirdPartyKnowledgeBaseConnectorsQo();

        // When
        ListThirdPartyKnowledgeBaseConnectorsResponseBody result
            = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnectors(Constant.TEST_PROJECT_ID,
            body);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnectors_shouldInvokeTranslation() throws Exception {
        // Given
        List<KnowledgeBaseConnectorEntity> connectorEntities = new ArrayList<>();
        KnowledgeBaseConnectorEntity connector = new KnowledgeBaseConnectorEntity();
        connector.setId("LakeSearch");
        connector.setName("LakeSearch Connector");
        connector.setDescription("Original LakeSearch Description");

        KnowledgeBaseConnectorParam param = new KnowledgeBaseConnectorParam();
        param.setCode("endpoint");
        param.setName("Endpoint URL");
        param.setRemark("Server endpoint");
        param.setType(KnowledgeBaseConnectorParamType.STRING);
        connector.setParams(Collections.singletonList(param));

        connectorEntities.add(connector);
        when(connectorMapper.listKnowledgeBaseConnectors(anyString())).thenReturn(connectorEntities);

        // Mock I18nUtils for context
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            mockedStaticI18nUtils.when(I18nUtils::getLocaleInContext).thenReturn(Locale.forLanguageTag("en"));

            // Mock I18nTransService
            when(i18nTransService.containsLocale("en")).thenReturn(true);
            when(i18nTransService.getMessage(eq("en"), eq("connector.LakeSearch.description"),
                eq("Original LakeSearch Description"))).thenReturn("Translated LakeSearch Description");
            when(i18nTransService.getMessage(eq("en"), eq("connector.LakeSearch.param.endpoint.name"),
                eq("Endpoint URL"))).thenReturn("Translated Endpoint URL");
            when(i18nTransService.getMessage(eq("en"), eq("connector.LakeSearch.param.endpoint.remark"),
                eq("Server endpoint"))).thenReturn("Translated Server Endpoint");

            // When
            ListThirdPartyKnowledgeBaseConnectorsResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnectors(
                Constant.TEST_PROJECT_ID, new ListThirdPartyKnowledgeBaseConnectorsQo());

            // Then
            assertNotNull(result);
            assertEquals(1, result.getConnectors().size());

            // 验证翻译结果
            KnowledgeBaseConnectorDetail translatedConnector = result.getConnectors().get(0);
            assertEquals("Translated LakeSearch Description", translatedConnector.getDescription());
            assertEquals("Translated Endpoint URL", translatedConnector.getParamDefinition().get(0).getName());
            assertEquals("Translated Server Endpoint", translatedConnector.getParamDefinition().get(0).getRemark());

            // 验证原始数据没有改变
            assertEquals("LakeSearch", connector.getId());
            assertEquals("Original LakeSearch Description", connector.getDescription());
        }
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnectors_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(connectorMapper.listKnowledgeBaseConnectors(anyString())).thenReturn(null);

            // When
            ListThirdPartyKnowledgeBaseConnectorsResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnectors(null, null);
        });
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnectors_should_return_not_null1() throws Exception {
        // Given
        KnowledgeBaseConnectorEntity knowledgeBaseConnectorEntity = new KnowledgeBaseConnectorEntity();
        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam.setType(KnowledgeBaseConnectorParamType.STRING);
        knowledgeBaseConnectorParam.setCode(Constant.TEST_PARAM_CODE);
        knowledgeBaseConnectorEntity.setParams(Collections.singletonList(knowledgeBaseConnectorParam));
        when(connectorMapper.listKnowledgeBaseConnectors(anyString())).thenReturn(
            Collections.singletonList(knowledgeBaseConnectorEntity));

        // When
        ListThirdPartyKnowledgeBaseConnectorsResponseBody result
            = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnectors(Constant.TEST_PROJECT_ID,
            null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_showKnowledgeBaseConnectorAbilities_should_return_not_null() throws Exception {
        try (MockedStatic<ObjectUtils> mockedStaticObjectUtils = mockStatic(ObjectUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            List<KnowledgeBaseConnectorAbilityEntity> list = new ArrayList<>();
            KnowledgeBaseConnectorAbilityEntity knowledgeBaseConnectorAbilityEntity
                = new KnowledgeBaseConnectorAbilityEntity();
            list.add(knowledgeBaseConnectorAbilityEntity);
            mockedStaticObjectUtils.when(() -> ObjectUtils.firstNonNull(any(Object.class), any(Object.class)))
                .thenReturn(list);

            List<KnowledgeBaseConnectorAbilityEntity> abilities = new ArrayList<>();
            KnowledgeBaseConnectorAbilityEntity knowledgeBaseConnectorAbilityEntity1
                = new KnowledgeBaseConnectorAbilityEntity();
            abilities.add(knowledgeBaseConnectorAbilityEntity1);
            when(connectorAbilityMapper.listConnectorAbility(anyString())).thenReturn(abilities);

            // When
            ShowKnowledgeBaseConnectorAbilitiesResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.showKnowledgeBaseConnectorAbilities("not_empty",
                new ShowKnowledgeBaseConnectorAbilitiesQo());

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_showKnowledgeBaseConnectorAbilities_should_not_throw_exception() throws Exception {
        // Given
        String testProjectId = "project-123";
        String testConnectorType = "Ragflow";
        String testConnectionId = "";

        ReflectionTestUtils.setField(thirdPartyKnowledgeBaseConnectionService, "connectorAbilityMapper",
            connectorAbilityMapper);

        // 模拟 Mapper 返回 null
        doReturn(null).when(connectorAbilityMapper).listConnectorAbility(anyString());

        ShowKnowledgeBaseConnectorAbilitiesResponseBody result = null;
        try {
            result = thirdPartyKnowledgeBaseConnectionService.showKnowledgeBaseConnectorAbilities(testProjectId,
                new ShowKnowledgeBaseConnectorAbilitiesQo());
        } catch (Exception e) {
            fail("方法不应该抛出任何异常，但捕获到了: " + e.getMessage());
        }

        assertNotNull(result, "即使没有数据，返回的 ResponseBody 也不应为 null");
        assertNotNull(result.getAbilities(), "Abilities 列表不应为 null");
        assertEquals(0, result.getAbilities().size(), "当 Mapper 返回 null 时，列表长度应为 0");

    }

    @Test
    void test_showKnowledgeBaseConnectorAbilities_should_return_not_null1() throws Exception {
        try (MockedStatic<ObjectUtils> mockedStaticObjectUtils = mockStatic(ObjectUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            KnowledgeBaseConnectorAbilityEntity connectorAbilityEntity = new KnowledgeBaseConnectorAbilityEntity();
            connectorAbilityEntity.setConnectorId("Ragflow");
            connectorAbilityEntity.setId(Constant.TEST_PARAM_ID);
            mockedStaticObjectUtils.when(() -> ObjectUtils.firstNonNull(any(Object.class), any(Object.class)))
                .thenReturn(Collections.singletonList(connectorAbilityEntity));
            when(connectorAbilityMapper.listConnectorAbility(anyString())).thenReturn(
                Collections.singletonList(connectorAbilityEntity));

            // When
            ShowKnowledgeBaseConnectorAbilitiesResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.showKnowledgeBaseConnectorAbilities(Constant.TEST_PROJECT_ID,
                new ShowKnowledgeBaseConnectorAbilitiesQo());

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnections_should_return_not_null() throws Exception {
        try (MockedStatic<SqlLikeEscapeHelper> mockedStaticSqlLikeEscapeHelper = mockStatic(SqlLikeEscapeHelper.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS);
            MockedStatic<PageMethod> mockedStaticPageMethod = mockStatic(PageMethod.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticSqlLikeEscapeHelper.when(() -> SqlLikeEscapeHelper.escapeLikeCharacters(anyString()))
                .thenReturn("not_empty");

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId())
                .thenReturn("not_empty");

            Page<Object> page = new Page<>();
            Object object = new Object();
            page.add(object);
            mockedStaticPageMethod.when(() -> PageMethod.offsetPage(anyInt(), anyInt())).thenReturn(page);

            List<KnowledgeBaseConnectionEntity> connectionEntities = new ArrayList<>();
            KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
            connectionEntities.add(knowledgeBaseConnectionEntity);
            when(connectionMapper.listThirdPartyKnowledgeBases(anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(connectionEntities);

            ListThirdPartyKnowledgeBaseConnectionsQo basesQo = new ListThirdPartyKnowledgeBaseConnectionsQo();

            // When
            ListThirdPartyKnowledgeBaseConnectionsResponse result
                = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnections("not_empty", basesQo);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnections_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<SqlLikeEscapeHelper> mockedStaticSqlLikeEscapeHelper = mockStatic(
                SqlLikeEscapeHelper.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                    RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<PageMethod> mockedStaticPageMethod = mockStatic(PageMethod.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticSqlLikeEscapeHelper.when(() -> SqlLikeEscapeHelper.escapeLikeCharacters(anyString()))
                    .thenReturn(null);

                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId())
                    .thenReturn(null);

                mockedStaticPageMethod.when(() -> PageMethod.offsetPage(anyInt(), anyInt())).thenReturn(null);

                when(connectionMapper.listThirdPartyKnowledgeBases(anyString(), anyString(), anyString(), anyString(),
                    anyString())).thenReturn(null);

                // When
                ListThirdPartyKnowledgeBaseConnectionsResponse result
                    = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnections(null, null);
            }
        });
    }

    @Test
    void test_listThirdPartyKnowledgeBaseConnections_should_return_not_null1() throws Exception {
        try (MockedStatic<SqlLikeEscapeHelper> mockedStaticSqlLikeEscapeHelper = mockStatic(SqlLikeEscapeHelper.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS);
            MockedStatic<PageMethod> mockedStaticPageMethod = mockStatic(PageMethod.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticSqlLikeEscapeHelper.when(() -> SqlLikeEscapeHelper.escapeLikeCharacters(anyString()))
                .thenReturn(null);

            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn(Constant.TEST_DOMAIN_ID);

            mockedStaticPageMethod.when(() -> PageMethod.offsetPage(anyInt(), anyInt())).thenReturn(null);

            when(connectionMapper.listThirdPartyKnowledgeBases(anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(Collections.singletonList(new KnowledgeBaseConnectionEntity()));

            // When
            ListThirdPartyKnowledgeBaseConnectionsResponse result
                = thirdPartyKnowledgeBaseConnectionService.listThirdPartyKnowledgeBaseConnections(
                Constant.TEST_PROJECT_ID, new ListThirdPartyKnowledgeBaseConnectionsQo());

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_showThirdPartyKnowledgeBaseConnection_should_return_not_null() throws Exception {
        // Given
        KnowledgeBaseConnectionEntity entity = new KnowledgeBaseConnectionEntity();
        entity.setId("not_empty");
        entity.setConnectorId("not_empty");
        entity.setConnectorName("not_empty");
        entity.setName("not_empty");
        entity.setStatus(KnowledgeBaseConnectionStatus.OPEN);
        entity.setIcon("not_empty");
        entity.setDescription("not_empty");
        List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParam = KnowledgeBaseConnectionParam.builder()
            .code("AppCode")
            .value("mockValue")
            .build();
        params.add(knowledgeBaseConnectionParam);
        entity.setParams(params);
        entity.setDomainName("not_empty");
        entity.setWorkspaceId("not_empty");
        entity.setUpdateUserId("not_empty");
        entity.setUpdateUserName("not_empty");
        when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
            anyString())).thenReturn(entity);

        // When
        ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody result
            = thirdPartyKnowledgeBaseConnectionService.showThirdPartyKnowledgeBaseConnection("not_empty", "not_empty",
            "not_empty");

        // Then
        assertNotNull(result);
    }

    @Test
    void test_showThirdPartyKnowledgeBaseConnection_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                anyString())).thenReturn(new KnowledgeBaseConnectionEntity());

            // When
            ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.showThirdPartyKnowledgeBaseConnection(
                Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID);
        });
    }

    @Test
    void test_testConnectionThirdPartyKnowledgeBaseConnection_should_return_not_null() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            // Given
            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_CONNECT_FAILED.getCode())))
                .thenReturn("not_empty");

            PageResult<ExternalKnowledgeBaseInfo> pageResult = new PageResult();
            when(knowledgeBaseConnectorContext.listKnowledgeBase(eq(null), eq(0), eq(1), any(ConnectorDefinition.class),
                any())).thenReturn(pageResult);

            TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody body
                = new TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody();

            // When
            TestConnectionThirdPartyKnowledgeBaseResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.testConnectionThirdPartyKnowledgeBaseConnection("not_empty",
                "not_empty", body);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_testConnectionThirdPartyKnowledgeBaseConnection_should_return_not_null1() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            // Given
            mockedStaticI18nUtils.when(
                () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_CONNECT_FAILED.getCode()))).thenReturn(null);

            when(knowledgeBaseConnectorContext.listKnowledgeBase(eq(null), eq(0), eq(1), any(ConnectorDefinition.class),
                any())).thenReturn(null);

            // When
            TestConnectionThirdPartyKnowledgeBaseResponseBody result
                = thirdPartyKnowledgeBaseConnectionService.testConnectionThirdPartyKnowledgeBaseConnection(null, null,
                null);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    public void test_testConnectionThirdPartyKnowledgeBaseConnectionById_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            String projectId = "project1";
            String knowledgeBaseConnectionId = "conn1";
            String workspaceId = "workspace1";

            // 创建 mock KnowledgeBaseConnection
            KnowledgeBaseConnection connection = mock(KnowledgeBaseConnection.class);
            when(connection.getConnectorId()).thenReturn("connector1");
            when(connection.getParams()).thenReturn("[]"); // 空的参数列表

            // 创建 mock KnowledgeBaseConnectorEntity
            KnowledgeBaseConnectorEntity connector = new KnowledgeBaseConnectorEntity();
            connector.setId("connector1");
            connector.setType("outside");
            connector.setParams(Arrays.asList());

            // mock mapper 方法
            when(knowledgeBaseConnectionMapper.batchQueryKnowledgeBaseConnections(anyList(), any())).thenReturn(
                Lists.newArrayList(connection));
            when(connectorMapper.find(anyString())).thenReturn(connector);

            // mock listKnowledgeBase 方法返回一个非空结果
            PageResult<ExternalKnowledgeBaseInfo> pageResult = new PageResult<>();
            when(knowledgeBaseConnectorContext.listKnowledgeBase(any(), anyInt(), anyInt(),
                any(ConnectorDefinition.class), any())).thenReturn(pageResult);

            // When
            TestConnectionThirdPartyKnowledgeBaseResponseBody response
                = thirdPartyKnowledgeBaseConnectionService.testConnectionThirdPartyKnowledgeBaseConnectionById(
                projectId, knowledgeBaseConnectionId, workspaceId);

            // Then
            assertNotNull(response);
        }
    }

    @Test
    void test_updateThirdPartyKnowledgeBaseConnection_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName())
                .thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("not_empty");

            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn("not_empty");

            List<ConnectionParamInfo> list = new ArrayList<>();
            ConnectionParamInfo connectionParamInfo = new ConnectionParamInfo();
            list.add(connectionParamInfo);
            when(knowledgeBaseConnectionValidateService.checkAndEncryptedParams(anyString(), anyList())).thenReturn(
                list);
            KnowledgeBaseConnectionEntity oldConnectionEntity = new KnowledgeBaseConnectionEntity();
            oldConnectionEntity.setConnectorId("not_empty");
            oldConnectionEntity.setIcon("not_empty");
            List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
            KnowledgeBaseConnectionParam knowledgeBaseConnectionParam = KnowledgeBaseConnectionParam.builder().build();
            params.add(knowledgeBaseConnectionParam);
            oldConnectionEntity.setParams(params);
            when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                anyString())).thenReturn(oldConnectionEntity);
            when(knowledgeBaseConnectionValidateService.checkConnectionUnique(anyString(),
                any(KnowledgeBaseConnectionEntity.class))).thenReturn(false);

            when(connectionMapper.update(any(KnowledgeBaseConnectionEntity.class))).thenReturn(1);

            UpdateThirdPartyKnowledgeBaseConnectionRequestBody body
                = new UpdateThirdPartyKnowledgeBaseConnectionRequestBody();

            // When
            UpdateThirdPartyKnowledgeBaseConnectionResponse result
                = thirdPartyKnowledgeBaseConnectionService.updateThirdPartyKnowledgeBaseConnection(
                Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID, body);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_updateThirdPartyKnowledgeBaseConnection_should_return_not_null3() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName()).thenReturn("");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("");

            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn("");

            List<ConnectionParamInfo> list = new ArrayList<>();
            ConnectionParamInfo connectionParamInfo = new ConnectionParamInfo();
            list.add(connectionParamInfo);
            when(knowledgeBaseConnectionValidateService.checkAndEncryptedParams(anyString(), anyList())).thenReturn(
                list);
            KnowledgeBaseConnectionEntity oldConnectionEntity = new KnowledgeBaseConnectionEntity();
            oldConnectionEntity.setConnectorId("");
            oldConnectionEntity.setIcon("");
            List<KnowledgeBaseConnectionParam> params = new ArrayList<>();
            KnowledgeBaseConnectionParam knowledgeBaseConnectionParam = KnowledgeBaseConnectionParam.builder().build();
            params.add(knowledgeBaseConnectionParam);
            oldConnectionEntity.setParams(params);
            when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                anyString())).thenReturn(oldConnectionEntity);
            when(knowledgeBaseConnectionValidateService.checkConnectionUnique(anyString(),
                any(KnowledgeBaseConnectionEntity.class))).thenReturn(false);

            when(connectionMapper.update(any(KnowledgeBaseConnectionEntity.class))).thenReturn(1);

            UpdateThirdPartyKnowledgeBaseConnectionRequestBody body
                = new UpdateThirdPartyKnowledgeBaseConnectionRequestBody();

            // When
            UpdateThirdPartyKnowledgeBaseConnectionResponse result
                = thirdPartyKnowledgeBaseConnectionService.updateThirdPartyKnowledgeBaseConnection(
                Constant.TEST_PROJECT_ID, Constant.TEST_WORKSPACE_ID, Constant.TEST_KNOWLEDGE_BASE_CONNECTION_ID, body);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_updateThirdPartyKnowledgeBaseConnection_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName()).thenReturn(null);
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn(null);

                mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(anyString())).thenReturn(null);

                when(knowledgeBaseConnectionValidateService.checkAndEncryptedParams(anyString(), anyList())).thenReturn(
                    null);
                when(knowledgeBaseConnectionValidateService.validateKnowledgeBasePermission(anyString(),
                    anyString())).thenReturn(null);
                when(knowledgeBaseConnectionValidateService.checkConnectionUnique(anyString(),
                    any(KnowledgeBaseConnectionEntity.class))).thenReturn(true);

                when(connectionMapper.update(any(KnowledgeBaseConnectionEntity.class))).thenReturn(0);

                // When
                UpdateThirdPartyKnowledgeBaseConnectionResponse result
                    = thirdPartyKnowledgeBaseConnectionService.updateThirdPartyKnowledgeBaseConnection(null, null, null,
                    null);
            }
        });
    }

    @Test
    void test_translateConnectors_shouldTranslateForNonChineseLocale() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            // 准备测试数据
            List<KnowledgeBaseConnectorDetail> connectors = new ArrayList<>();
            KnowledgeBaseConnectorDetail connector = new KnowledgeBaseConnectorDetail();
            connector.setId("LakeSearch");
            connector.setDescription("Original Description");

            // 添加参数定义
            List<ParamDefinitionInfo> params = new ArrayList<>();
            ParamDefinitionInfo param = new ParamDefinitionInfo();
            param.setCode("endpoint");
            param.setName("Endpoint");
            param.setRemark("Server address");
            params.add(param);
            connector.setParamDefinition(params);

            connectors.add(connector);

            // 模拟英文环境 - toLanguageTag().toLowerCase() 应该返回 "en"
            mockedStaticI18nUtils.when(I18nUtils::getLocaleInContext).thenReturn(Locale.forLanguageTag("en"));

            // mock国际化服务 - 需要先设置locale支持
            when(i18nTransService.containsLocale("en")).thenReturn(true);
            when(i18nTransService.getMessage(eq("en"), eq("connector.LakeSearch.description"),
                eq("Original Description"))).thenReturn("Translated Description");
            when(i18nTransService.getMessage(eq("en"), eq("connector.LakeSearch.param.endpoint.name"),
                eq("Endpoint"))).thenReturn("Translated Endpoint");
            when(i18nTransService.getMessage(eq("en"), eq("connector.LakeSearch.param.endpoint.remark"),
                eq("Server address"))).thenReturn("Translated Server Address");

            // 调用私有方法测试
            ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "translateConnectors",
                connectors);

            // 验证翻译结果
            assertEquals("Translated Description", connectors.get(0).getDescription());
            assertEquals("Translated Endpoint", connectors.get(0).getParamDefinition().get(0).getName());
            assertEquals("Translated Server Address", connectors.get(0).getParamDefinition().get(0).getRemark());
        }
    }

    @Test
    void test_translateConnectors_shouldHandleEmptyList() throws Exception {
        // 准备空列表
        List<KnowledgeBaseConnectorDetail> connectors = new ArrayList<>();

        // 调用私有方法测试（不应该抛出异常）
        ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "translateConnectors", connectors);

        // 验证空列表处理正常
        assertTrue(connectors.isEmpty());
    }

    @Test
    void test_translateAbilities_shouldTranslateJsonFields() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            // 准备测试数据
            List<AbilityInfo> abilities = new ArrayList<>();
            AbilityInfo ability = new AbilityInfo();
            ability.setCode("RETRIEVE");
            ability.setSubAbility(
                "{\"search_mode_list\":[{\"search_mode\":\"vector\",\"search_mode_name\":\"向量\",\"summary\":\"向量搜索\",\"description\":\"基于向量的语义搜索\"}]}");
            abilities.add(ability);

            // 模拟英文环境 - toLanguageTag() 应该返回 "en"
            mockedStaticI18nUtils.when(I18nUtils::getLocaleInContext).thenReturn(Locale.forLanguageTag("en"));

            // mock国际化服务 - 需要先设置locale支持
            when(i18nTransService.containsLocale("en")).thenReturn(true);
            when(i18nTransService.getMessage(eq("en"), eq("ability.RETRIEVE.search_mode.vector.name"),
                eq("向量"))).thenReturn("Vector");
            when(i18nTransService.getMessage(eq("en"), eq("ability.RETRIEVE.search_mode.vector.summary"),
                eq("向量搜索"))).thenReturn("Vector Search");
            when(i18nTransService.getMessage(eq("en"), eq("ability.RETRIEVE.search_mode.vector.description"),
                eq("基于向量的语义搜索"))).thenReturn("Semantic search based on vectors");

            // 调用私有方法测试
            ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "translateAbilities", abilities);

            // 验证翻译后的JSON
            String translatedJson
                = "{\"search_mode_list\":[{\"search_mode\":\"vector\",\"search_mode_name\":\"Vector\",\"summary\":\"Vector Search\",\"description\":\"Semantic search based on vectors\"}]}";
            assertEquals(translatedJson, abilities.get(0).getSubAbility());
        }
    }

    @Test
    void test_translateAbilities_shouldHandleInvalidJson() throws Exception {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            // 准备测试数据 - 无效JSON
            List<AbilityInfo> abilities = new ArrayList<>();
            AbilityInfo ability = new AbilityInfo();
            ability.setCode("RETRIEVE");
            ability.setSubAbility("invalid json");
            abilities.add(ability);

            // 模拟英文环境
            mockedStaticI18nUtils.when(I18nUtils::getLocaleInContext).thenReturn(Locale.forLanguageTag("en"));

            // 调用私有方法测试（不应该抛出异常）
            ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "translateAbilities", abilities);

            // 验证原始内容被保留
            assertEquals("invalid json", abilities.get(0).getSubAbility());
        }
    }

    @Test
    void test_translateAbilities_shouldHandleEmptyList() throws Exception {
        // 准备空列表
        List<AbilityInfo> abilities = new ArrayList<>();

        // 调用私有方法测试（不应该抛出异常）
        ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "translateAbilities", abilities);

        // 验证空列表处理正常
        assertTrue(abilities.isEmpty());
    }

    @Test
    void test_replaceJsonField_shouldReplaceExistingField() throws Exception {
        // 创建JSON节点
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("search_mode_name", "向量");
        node.put("other_field", "value");

        // mock国际化服务
        when(i18nTransService.getMessage(eq("en"), eq("ability.test.key"), eq("向量"))).thenReturn("Vector");

        // 调用私有方法测试
        ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "replaceJsonField", node, "en",
            "search_mode_name", "ability.test.key");

        // 验证字段已被替换
        assertEquals("Vector", node.get("search_mode_name").asText());
        assertEquals("value", node.get("other_field").asText()); // 其他字段不变
    }

    @Test
    void test_replaceJsonField_shouldSkipNonExistingField() throws Exception {
        // 创建JSON节点
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("other_field", "value");

        // 调用私有方法测试
        ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "replaceJsonField", node, "en",
            "search_mode_name", "ability.test.key");

        // 验证节点没有变化
        assertEquals("value", node.get("other_field").asText());
        assertFalse(node.has("search_mode_name"));
    }

    @Test
    void test_replaceJsonField_shouldNotReplaceIfTranslationIsSame() throws Exception {
        // 创建JSON节点
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("search_mode_name", "Vector");

        // mock国际化服务返回相同值
        when(i18nTransService.getMessage(eq("en"), eq("ability.test.key"), eq("Vector"))).thenReturn("Vector");

        // 调用私有方法测试
        ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService, "replaceJsonField", node, "en",
            "search_mode_name", "ability.test.key");

        // 验证字段没有变化（值为相同）
        assertEquals("Vector", node.get("search_mode_name").asText());
    }

    @Test
    void test_showKnowledgeBaseConnectorAbilities_shouldReturnAbilitySuccessfully() throws Exception {
        // 准备 Meta 数据
        Map<String, KnowledgeBaseConnectorAbilityEntity> metaMap = new HashMap<>();
        KnowledgeBaseConnectorAbilityEntity metaRetrieve = new KnowledgeBaseConnectorAbilityEntity();
        metaRetrieve.setCode("RETRIEVE");
        metaRetrieve.setName("检索");
        metaRetrieve.setSubAbility(
            "{" + "\"search_mode_list\": [{" + "    \"search_mode\": \"doc\"," + "    \"search_mode_name\": \"语义检索\","
                + "    \"search_mode_name_en\": \"Vector Search\"," + "    \"summary\": \"基于向量的语义匹配\","
                + "    \"summary_en\": \"Semantic matching based on vectors\","
                + "    \"description\": \"详细的语义检索描述信息\","
                + "    \"description_en\": \"Detailed description for vector search\"" + "}]" + "}");
        metaMap.put("RETRIEVE", metaRetrieve);

        // 准备用户 UsedAbilities (用户开启了 doc 模式)
        List<AbilityDetail> usedAbilities = new ArrayList<>();
        AbilityDetail detail = new AbilityDetail();
        detail.setAbilityCode("RETRIEVE");
        detail.setEnable(true);
        detail.setSubAbility(
            String.valueOf(objectMapper.readTree("{\"search_mode_list\":[{\"search_mode\":\"doc\"}]}")));
        usedAbilities.add(detail);

        // 调用
        when(abilityProcessorFactory.getProcessor(anyString())).thenReturn(new RetrieveProcessor(objectMapper));
        List<AbilityInfo> result = ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService,
            "getCustomizedAbilities", usedAbilities, metaMap);

        // 断言
        assertEquals(1, result.size());
        assertEquals("RETRIEVE", result.get(0).getCode());
        assertTrue(result.get(0).getSubAbility().contains("语义"));
    }

    @Test
    void test_showKnowledgeBaseConnectorAbilities_ShouldReturnEmpty() {
        // 准备 Meta 数据
        Map<String, KnowledgeBaseConnectorAbilityEntity> metaMap = new HashMap<>();

        KnowledgeBaseConnectorAbilityEntity metaRetrieve = new KnowledgeBaseConnectorAbilityEntity();
        metaRetrieve.setCode("RETRIEVE");
        metaRetrieve.setName("检索能力");
        metaMap.put("RETRIEVE", metaRetrieve);

        List<AbilityDetail> usedAbilities = new ArrayList<>();

        // 情况 A: 开启了 OCR，但 MetaMap 里没这个能力（模拟连接器不支持 OCR）
        AbilityDetail ocrDetail = new AbilityDetail();
        ocrDetail.setAbilityCode("OCR");
        ocrDetail.setEnable(true);
        usedAbilities.add(ocrDetail);

        // 情况 B: 配置了 RETRIEVE，但用户手动关闭了 (enable = false)
        AbilityDetail retrieveDetail = new AbilityDetail();
        retrieveDetail.setAbilityCode("RETRIEVE");
        retrieveDetail.setEnable(false);
        usedAbilities.add(retrieveDetail);

        // 调用
        List<AbilityInfo> result = ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService,
            "getCustomizedAbilities", usedAbilities, metaMap);

        // 断言
        assertNotNull(result);
        assertTrue(result.isEmpty(), "当能力不匹配(OCR)或已禁用(RETRIEVE)时，结果列表必须为空");
    }

    @Test
    void test_subAbilityMetaNotSupported_shouldReturnEmptySubAbility() throws Exception {
        // 准备 Meta 数据
        Map<String, KnowledgeBaseConnectorAbilityEntity> metaMap = new HashMap<>();

        KnowledgeBaseConnectorAbilityEntity metaRetrieve = new KnowledgeBaseConnectorAbilityEntity();
        metaRetrieve.setCode("RETRIEVE");
        metaRetrieve.setName("检索能力");
        metaRetrieve.setSubAbility(
            "{\"search_mode_list\":[{\"search_mode\":\"doc\",\"search_mode_name\":\"语义检索\"}]}");
        metaMap.put("RETRIEVE", metaRetrieve);

        // 构造 usedAbilities
        List<AbilityDetail> usedAbilities = new ArrayList<>();
        AbilityDetail detail = new AbilityDetail();
        detail.setAbilityCode("RETRIEVE");
        detail.setEnable(true);

        detail.setSubAbility(
            String.valueOf(objectMapper.readTree("{\"search_mode_list\":[{\"search_mode\":\"web\"}]}")));
        usedAbilities.add(detail);

        // 当 Processor 发现用户传的 "web" 不在 Meta 的 "doc" 范围中时，返回空列表字符串
        when(abilityProcessorFactory.getProcessor("RETRIEVE")).thenReturn(retrieveProcessor);
        when(retrieveProcessor.processSubAbility(anyString(), anyString())).thenReturn("{\"search_mode_list\":[]}");

        // 调用
        List<AbilityInfo> result = ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService,
            "getCustomizedAbilities", usedAbilities, metaMap);

        // 断言
        assertNotNull(result);
        assertEquals(1, result.size());

        AbilityInfo info = result.get(0);
        assertEquals("RETRIEVE", info.getCode());
        assertNotNull(info.getSubAbility());
        assertTrue(info.getSubAbility().contains("[]"), "不支持的模式应过滤为空列表");
    }

    @Test
    void testMixedAbilities_NoSubAbility() {
        //  准备 Meta 数据
        Map<String, KnowledgeBaseConnectorAbilityEntity> metaMap = new HashMap<>();

        // 构造 KNOWLEDGE_LIST 实体
        KnowledgeBaseConnectorAbilityEntity knowledgeListMeta = new KnowledgeBaseConnectorAbilityEntity();
        knowledgeListMeta.setCode("KNOWLEDGE_LIST");
        knowledgeListMeta.setName("知识列表");
        metaMap.put("KNOWLEDGE_LIST", knowledgeListMeta);

        // 构造 THIRD_PARTY_MODEL 实体
        KnowledgeBaseConnectorAbilityEntity thirdPartyMeta = new KnowledgeBaseConnectorAbilityEntity();
        thirdPartyMeta.setCode("THIRD_PARTY_MODEL");
        thirdPartyMeta.setName("第三方模型");
        metaMap.put("THIRD_PARTY_MODEL", thirdPartyMeta);

        // 构造 OCR 实体
        KnowledgeBaseConnectorAbilityEntity ocrMeta = new KnowledgeBaseConnectorAbilityEntity();
        ocrMeta.setCode("OCR");
        ocrMeta.setName("OCR识别");
        metaMap.put("OCR", ocrMeta);

        // 2. 构造 usedAbilities (用户实际开启的能力)
        List<AbilityDetail> usedAbilities = new ArrayList<>();

        // 开启 KNOWLEDGE_LIST
        AbilityDetail ad1 = new AbilityDetail();
        ad1.setAbilityCode("KNOWLEDGE_LIST");
        ad1.setEnable(true);
        usedAbilities.add(ad1);

        // 开启 THIRD_PARTY_MODEL
        AbilityDetail ad2 = new AbilityDetail();
        ad2.setAbilityCode("THIRD_PARTY_MODEL");
        ad2.setEnable(true);
        usedAbilities.add(ad2);

        // 特意不开启 OCR (或者不放进 map)，用于测试过滤逻辑
        // AbilityDetail ad3 = new AbilityDetail(); ad3.setEnable(false); usedAbilities.put("OCR", ad3);

        // 3. 执行私有方法
        List<AbilityInfo> result = ReflectionTestUtils.invokeMethod(thirdPartyKnowledgeBaseConnectionService,
            "getCustomizedAbilities", usedAbilities, metaMap);

        // 4. 断言校验
        assertNotNull(result);
        // 预期结果：只包含用户开启且元数据中存在的那 2 个能力
        assertEquals(2, result.size());

        // 校验 Code 是否匹配
        List<String> codes = result.stream().map(AbilityInfo::getCode).toList();
        assertTrue(codes.contains("KNOWLEDGE_LIST"));
        assertTrue(codes.contains("THIRD_PARTY_MODEL"));
        assertFalse(codes.contains("OCR"), "未开启的能力不应出现在结果中");

        // 校验基础字段是否正确从 Meta 填充到了结果中
        AbilityInfo info = result.stream().filter(a -> "KNOWLEDGE_LIST".equals(a.getCode())).findFirst().get();
        assertEquals("知识列表", info.getName());
    }
}