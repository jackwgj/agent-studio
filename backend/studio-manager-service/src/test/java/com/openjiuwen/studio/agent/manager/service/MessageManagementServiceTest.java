/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.StructuredInfoRequest;
import com.openjiuwen.studio.agent.manager.dto.StructuredInfoRequestDelete;
import com.openjiuwen.studio.agent.manager.entity.StructuredMessageEntity;
import com.openjiuwen.studio.agent.manager.mapper.StructuredMessageMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class MessageManagementServiceTest {

    @Mock
    private StructuredMessageMapper structuredMessageMapper;

    @Mock
    private I18nUtil i18nUtil;

    private MessageManagementService messageManagementService;

    @BeforeEach
    void setUp() {
        messageManagementService = new MessageManagementService(structuredMessageMapper, i18nUtil);
    }

    @Test
    void testValidateStructuredMessagesName_ValidName() {
        messageManagementService.validateStructuredMessagesName("TestMessage01");
    }

    @Test
    void testValidateStructuredMessagesName_ChineseName() {
        messageManagementService.validateStructuredMessagesName("测试消息模板");
    }

    @Test
    void testValidateStructuredMessagesName_NullName() {
        assertThrows(AgentStudioException.class,
            () -> messageManagementService.validateStructuredMessagesName(null));
    }

    @Test
    void testValidateStructuredMessagesName_EmptyName() {
        assertThrows(AgentStudioException.class,
            () -> messageManagementService.validateStructuredMessagesName(""));
    }

    @Test
    void testValidateStructuredMessagesName_TooShort() {
        assertThrows(AgentStudioException.class,
            () -> messageManagementService.validateStructuredMessagesName("a"));
    }

    @Test
    void testValidateStructuredMessagesName_StartsWithSpace() {
        assertThrows(AgentStudioException.class,
            () -> messageManagementService.validateStructuredMessagesName(" test"));
    }

    @Test
    void testValidateStructuredMessagesName_EndsWithSpace() {
        assertThrows(AgentStudioException.class,
            () -> messageManagementService.validateStructuredMessagesName("test "));
    }

    @Test
    void testDeleteStructuredMessages_EmptyItems() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            List<StructuredInfoRequestDelete> body = new ArrayList<>();

            DatasourceBatchDeleteRsp result = messageManagementService.deleteStructuredMessages("proj-1", "ws-1", body);
            assertNotNull(result);
            assertEquals(0L, result.getCount());
        }
    }

    @Test
    void testDeleteStructuredMessages_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");

            StructuredInfoRequestDelete deleteReq = new StructuredInfoRequestDelete();
            deleteReq.setId("msg-1");
            List<StructuredInfoRequestDelete> body = List.of(deleteReq);

            StructuredMessageEntity entity = new StructuredMessageEntity();
            entity.setId("msg-1");
            when(structuredMessageMapper.getByIds(any(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(entity));

            DatasourceBatchDeleteRsp result = messageManagementService.deleteStructuredMessages("proj-1", "ws-1", body);
            assertNotNull(result);
            assertEquals(1L, result.getCount());
        }
    }

    @Test
    void testDeleteStructuredMessages_SizeMismatch() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");

            StructuredInfoRequestDelete deleteReq1 = new StructuredInfoRequestDelete();
            deleteReq1.setId("msg-1");
            StructuredInfoRequestDelete deleteReq2 = new StructuredInfoRequestDelete();
            deleteReq2.setId("msg-2");
            List<StructuredInfoRequestDelete> body = List.of(deleteReq1, deleteReq2);

            StructuredMessageEntity entity = new StructuredMessageEntity();
            entity.setId("msg-1");
            when(structuredMessageMapper.getByIds(any(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(entity));

            assertThrows(AgentStudioException.class,
                () -> messageManagementService.deleteStructuredMessages("proj-1", "ws-1", body));
        }
    }

    @Test
    void testGetStructuredMessagesCurrentNames_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            StructuredMessageEntity entity1 = new StructuredMessageEntity();
            entity1.setId("id-1");
            entity1.setName("Message1");
            StructuredMessageEntity entity2 = new StructuredMessageEntity();
            entity2.setId("id-2");
            entity2.setName("Message2");

            when(structuredMessageMapper.getStructuredMessageEntity(any(), eq("desc")))
                .thenReturn(List.of(entity1, entity2));

            var result = messageManagementService.getStructuredMessagesCurrentNames("proj-1", "ws-1", null);
            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    @Test
    void testGetStructuredMessagesCurrentNames_WithExcludeId() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");

            StructuredMessageEntity entity1 = new StructuredMessageEntity();
            entity1.setId("id-1");
            entity1.setName("Message1");
            StructuredMessageEntity entity2 = new StructuredMessageEntity();
            entity2.setId("id-2");
            entity2.setName("Message2");

            when(structuredMessageMapper.getStructuredMessageEntity(any(), eq("desc")))
                .thenReturn(List.of(entity1, entity2));

            var result = messageManagementService.getStructuredMessagesCurrentNames("proj-1", "ws-1", "id-1");
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }
}
