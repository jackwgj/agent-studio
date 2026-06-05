/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeShareScopeEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeShareScopeMapper;
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
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class ShareScopeValidUtilTest {
    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeShareScopeMapper knowledgeShareScopeMapper;

    @InjectMocks
    private ShareScopeValidUtil scopeValidUtil;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scopeValidUtil, "knowledgeShareScopeMapper", knowledgeShareScopeMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_checkKnowledgeScope_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                    .thenReturn("not_empty");

                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .workspaceId("not_empty")
                    .shareScope("GLOBAL")
                    .build();
                // When
                ShareScopeValidUtil.checkKnowledgeScope("not_empty", "not_empty", knowledgeBaseEntity);
            }
        });
    }

    @Test
    void test_partial_checkKnowledgeScope_not_should_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                    .thenReturn(null);
                KnowledgeShareScopeEntity knowledgeShareScopeEntity = new KnowledgeShareScopeEntity();
                knowledgeShareScopeEntity.setId("not_empty");
                knowledgeShareScopeEntity.setWorkspaceId("not_empty");
                when(knowledgeShareScopeMapper.queryShareScope(any(), any())).thenReturn(knowledgeShareScopeEntity);
                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .workspaceId("not_empty")
                    .shareScope("PARTIAL")
                    .build();

                // When
                ShareScopeValidUtil.checkKnowledgeScope("not_empty", "not_empty", knowledgeBaseEntity);
            }
        });

    }

    @Test
    void test_partial_checkKnowledgeScope_should_throw_exception() throws Exception {
        assertThrows(AgentBaseException.class, () -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                    .thenReturn(null);
                KnowledgeShareScopeEntity knowledgeShareScopeEntity = new KnowledgeShareScopeEntity();
                knowledgeShareScopeEntity.setId("not_empty");
                knowledgeShareScopeEntity.setWorkspaceId("empty");
                when(knowledgeShareScopeMapper.queryShareScope(any(), any())).thenReturn(knowledgeShareScopeEntity);
                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .workspaceId("not_empty")
                    .shareScope("PARTIAL")
                    .build();

                // When
                ShareScopeValidUtil.checkKnowledgeScope("not_empty", "not_empty", knowledgeBaseEntity);
            }
        });
    }

    @Test
    void test_global_checkKnowledgeScope_should_throw_exception() throws Exception {
        assertThrows(AgentBaseException.class, () -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                    .thenReturn(null);

                KnowledgeBaseEntity knowledgeBaseEntity = KnowledgeBaseEntity.builder()
                    .workspaceId("not_empty")
                    .shareScope("SELF")
                    .build();

                // When
                ShareScopeValidUtil.checkKnowledgeScope("not_empty", "empty", knowledgeBaseEntity);
            }
        });

    }

    @Test
    void test_checkKnowledgeScope_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticI18nUtils.when(
                        () -> I18nUtils.getMessage(eq(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST.getCode()), anyString()))
                    .thenReturn(null);

                KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
                knowledgeBaseEntity.setShareScope("SELF");
                knowledgeBaseEntity.setWorkspaceId("workspace_id");
                // When
                ShareScopeValidUtil.checkKnowledgeScope("test-id", "workspace_id", knowledgeBaseEntity);
            }
        });

    }
}