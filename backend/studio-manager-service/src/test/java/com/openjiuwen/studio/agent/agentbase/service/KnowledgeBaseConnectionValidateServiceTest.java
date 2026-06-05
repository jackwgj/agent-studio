/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorEntity;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectionStatus;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectorParamType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectorMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectorParam;
import com.openjiuwen.studio.agent.agentbase.service.validate.KnowledgeBaseConnectionValidateService;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.manager.dto.ConnectionParamInfo;

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
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
public class KnowledgeBaseConnectionValidateServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectionMapper connectionMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseConnectorMapper connectorMapper;

    private AutoCloseable mockitoCloseable;

    @InjectMocks
    private KnowledgeBaseConnectionValidateService knowledgeBaseConnectionValidateService;

    private static MockedStatic<CryptoUtils> mockedStaticCryptoUtils;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(knowledgeBaseConnectionValidateService, "connectionMapper", connectionMapper);
        ReflectionTestUtils.setField(knowledgeBaseConnectionValidateService, "connectorMapper", connectorMapper);

        mockedStaticCryptoUtils = mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();

        if (mockedStaticCryptoUtils != null) {
            mockedStaticCryptoUtils.close();
        }
    }

    @Test
    void test_checkAndEncryptedParams_with_not_null() {
        //arrange
        KnowledgeBaseConnectorEntity connectorEntity = new KnowledgeBaseConnectorEntity();
        KnowledgeBaseConnectorParam connectorParam = new KnowledgeBaseConnectorParam();
        connectorParam.setCode("code");
        connectorParam.setNeed("Y");
        connectorParam.setType(KnowledgeBaseConnectorParamType.STRING);
        connectorParam.setPattern("pattern");
        connectorEntity.setParams(List.of(connectorParam));

        ConnectionParamInfo connectionParamInfo = new ConnectionParamInfo();
        connectionParamInfo.setCode("code");
        connectionParamInfo.setValue("");

        when(connectorMapper.find(anyString())).thenReturn(connectorEntity);

        //act
        //assert
        assertThrows(AgentBaseException.class,
            () -> knowledgeBaseConnectionValidateService.checkAndEncryptedParams("connectorId",
                List.of(connectionParamInfo)));
    }

    @Test
    void test_checkAndEncryptedParams_with_not_null1() {
        //arrange
        KnowledgeBaseConnectorEntity connectorEntity = new KnowledgeBaseConnectorEntity();
        KnowledgeBaseConnectorParam connectorParam = new KnowledgeBaseConnectorParam();
        connectorParam.setCode("code");
        connectorParam.setNeed("Y");
        connectorParam.setType(KnowledgeBaseConnectorParamType.STRING);
        connectorParam.setPattern("pattern");
        connectorEntity.setParams(List.of(connectorParam));

        ConnectionParamInfo connectionParamInfo = new ConnectionParamInfo();
        connectionParamInfo.setCode("code");
        connectionParamInfo.setValue("value");

        when(connectorMapper.find(anyString())).thenReturn(connectorEntity);

        List<ConnectionParamInfo> connectionParamInfos = knowledgeBaseConnectionValidateService.checkAndEncryptedParams(
            "His", List.of(connectionParamInfo));
        assertNotNull(connectionParamInfos);
    }

    @Test
    void test_cleanSensitiveInformation_should_clean_appcode_sensitive_info() {

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

        // When
        knowledgeBaseConnectionValidateService.cleanSensitiveInformation(entity);

        // Then - assert
        assertNotNull(entity.getParams());
        assertEquals(1, entity.getParams().size());
        assertEquals("AppCode", entity.getParams().get(0).getCode());
        assertEquals("******", entity.getParams().get(0).getValue());
    }

    @Test
    void testGetSameConnection() {
        // Given
        String workSpaceId = "workspace123";
        KnowledgeBaseConnectionEntity inputEntity = new KnowledgeBaseConnectionEntity();
        inputEntity.setConnectorId("connector1");
        inputEntity.setParams(Arrays.asList(new KnowledgeBaseConnectionParam("param1", "encryptedValue1"),
            new KnowledgeBaseConnectionParam("param2", "value2")));

        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam1 = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam1.setCode("param1");
        knowledgeBaseConnectorParam1.setType(KnowledgeBaseConnectorParamType.SECRET);
        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam2 = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam2.setCode("param2");
        knowledgeBaseConnectorParam2.setType(KnowledgeBaseConnectorParamType.STRING);
        // Mock connector
        KnowledgeBaseConnectorEntity mockConnector = new KnowledgeBaseConnectorEntity();
        mockConnector.setId("connector1");
        mockConnector.setType("outside");
        mockConnector.setParams(Arrays.asList(knowledgeBaseConnectorParam1, knowledgeBaseConnectorParam2));

        // Mock connection mapper
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId("conn1");
        knowledgeBaseConnectionEntity.setConnectorId("connector1");
        knowledgeBaseConnectionEntity.setParams(
            Arrays.asList(new KnowledgeBaseConnectionParam("param1", "decryptedValue1"),
                new KnowledgeBaseConnectionParam("param2", "value2")));
        List<KnowledgeBaseConnectionEntity> existingConnections = Arrays.asList(knowledgeBaseConnectionEntity);

        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {

            mockedRequestContext.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("domain123");

            mockedStaticCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypted_value");

            when(connectorMapper.find(any(String.class))).thenReturn(mockConnector);
            when(connectionMapper.listThirdPartyKnowledgeBases(any(), any(), any(), any(), any())).thenReturn(
                existingConnections);

            // When
            KnowledgeBaseConnectionEntity result = knowledgeBaseConnectionValidateService.getSameConnection(workSpaceId,
                inputEntity);

            // Then
            assertNotNull(result);
            assertEquals("conn1", result.getId());
        }
    }

    @Test
    void testGetSameConnectionNoMatch() {
        // Given
        String workSpaceId = "workspace123";
        KnowledgeBaseConnectionEntity inputEntity = new KnowledgeBaseConnectionEntity();
        inputEntity.setConnectorId("connector1");
        inputEntity.setParams(Arrays.asList(new KnowledgeBaseConnectionParam("param1", "encryptedValue1"),
            new KnowledgeBaseConnectionParam("param2", "value2")));

        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam1 = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam1.setCode("param1");
        knowledgeBaseConnectorParam1.setType(KnowledgeBaseConnectorParamType.SECRET);
        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam2 = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam2.setCode("param2");
        knowledgeBaseConnectorParam2.setType(KnowledgeBaseConnectorParamType.STRING);

        KnowledgeBaseConnectorEntity mockConnector = new KnowledgeBaseConnectorEntity();
        mockConnector.setId("connector1");
        mockConnector.setType("outside");
        mockConnector.setParams(Arrays.asList(knowledgeBaseConnectorParam1, knowledgeBaseConnectorParam2));

        // Mock connection mapper - no existing connections
        List<KnowledgeBaseConnectionEntity> existingConnections = Collections.emptyList();

        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("domain123");
            when(CryptoUtils.decrypt(any())).thenReturn("decrypted_value");
            when(connectorMapper.find(any(String.class))).thenReturn(mockConnector);
            when(connectionMapper.listThirdPartyKnowledgeBases(any(), any(), any(), any(), any())).thenReturn(
                existingConnections);

            // When
            KnowledgeBaseConnectionEntity result = knowledgeBaseConnectionValidateService.getSameConnection(workSpaceId,
                inputEntity);

            // Then
            assertNull(result);
        }
    }

    @Test
    void testGetSameConnectionWithUndefinedParam() {
        // Given
        String workSpaceId = "workspace123";
        KnowledgeBaseConnectionEntity inputEntity = new KnowledgeBaseConnectionEntity();
        inputEntity.setConnectorId("connector1");
        inputEntity.setParams(Arrays.asList(new KnowledgeBaseConnectionParam("param1", "encryptedValue1"),
            new KnowledgeBaseConnectionParam("param3", "value3") // 未定义的参数
        ));

        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam1 = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam1.setCode("param1");
        knowledgeBaseConnectorParam1.setType(KnowledgeBaseConnectorParamType.SECRET);

        KnowledgeBaseConnectorEntity mockConnector = new KnowledgeBaseConnectorEntity();
        mockConnector.setId("connector1");
        mockConnector.setType("outside");
        mockConnector.setParams(Arrays.asList(knowledgeBaseConnectorParam1));

        KnowledgeBaseConnectionEntity existingConnection = new KnowledgeBaseConnectionEntity();
        existingConnection.setId("conn1");
        existingConnection.setConnectorId("connector1");
        existingConnection.setParams(Arrays.asList(new KnowledgeBaseConnectionParam("param1", "decryptedValue1")));

        List<KnowledgeBaseConnectionEntity> existingConnections = Arrays.asList(existingConnection);

        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("domain123");

            mockedStaticCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypted_value");

            when(connectorMapper.find(any(String.class))).thenReturn(mockConnector);
            when(connectionMapper.listThirdPartyKnowledgeBases(any(), any(), any(), any(), any())).thenReturn(
                existingConnections);

            // When
            KnowledgeBaseConnectionEntity result = knowledgeBaseConnectionValidateService.getSameConnection(workSpaceId,
                inputEntity);

            // Then
            assertNotNull(result);
            assertEquals("conn1", result.getId());
        }
    }

    @Test
    void testGetSameConnectionStringParamNotMatch() {
        // Given
        String workSpaceId = "workspace123";
        KnowledgeBaseConnectionEntity inputEntity = new KnowledgeBaseConnectionEntity();
        inputEntity.setConnectorId("connector1");
        inputEntity.setParams(Arrays.asList(new KnowledgeBaseConnectionParam("param2", "value2")));

        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam2 = new KnowledgeBaseConnectorParam();
        knowledgeBaseConnectorParam2.setCode("param2");
        knowledgeBaseConnectorParam2.setType(KnowledgeBaseConnectorParamType.STRING);

        KnowledgeBaseConnectorEntity mockConnector = new KnowledgeBaseConnectorEntity();
        mockConnector.setId("connector1");
        mockConnector.setType("outside");
        mockConnector.setParams(Arrays.asList(knowledgeBaseConnectorParam2));

        KnowledgeBaseConnectionEntity existingConnection = new KnowledgeBaseConnectionEntity();
        existingConnection.setId("conn1");
        existingConnection.setConnectorId("connector1");
        existingConnection.setParams(Arrays.asList(new KnowledgeBaseConnectionParam("param2", "differentValue")));

        List<KnowledgeBaseConnectionEntity> existingConnections = Arrays.asList(existingConnection);

        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("domain123");
            when(connectorMapper.find(any(String.class))).thenReturn(mockConnector);
            when(connectionMapper.listThirdPartyKnowledgeBases(any(), any(), any(), any(), any())).thenReturn(
                existingConnections);

            // When
            KnowledgeBaseConnectionEntity result = knowledgeBaseConnectionValidateService.getSameConnection(workSpaceId,
                inputEntity);

            // Then
            assertNull(result);
        }
    }

    @Test
    void test_checkThirdPartyKnowledgeBaseConnectionName_throw_exception() {
        assertThrows(AgentBaseException.class, () -> {
            when(connectionMapper.findByName(anyString(), anyString(), anyString())).thenReturn("name");
            knowledgeBaseConnectionValidateService.checkThirdPartyKnowledgeBaseConnectionName("workspaceId", "name",
                "knowledgeBaseConnectionId");
        });
    }
}
