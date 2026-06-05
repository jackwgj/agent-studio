/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.debugging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.dto.agent.ConversationInfo;
import com.openjiuwen.studio.agent.runtime.model.debugging.ControllerExecutionBriefModel;

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
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class ControllerDebuggingMgmtServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @InjectMocks
    private ControllerDebuggingMgmtService controllerDebuggingMgmtService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(controllerDebuggingMgmtService, "redisClient", redisClient);
        ReflectionTestUtils.setField(controllerDebuggingMgmtService, "expireTime", 0L);
        ReflectionTestUtils.setField(controllerDebuggingMgmtService, "maxExecutionSize", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_clearAgentConversationRecords_empty_conversations() {
        assertDoesNotThrow(() -> {
            String userId = "user1";
            String agentId = "agent1";
            List<ConversationInfo> needDeleteConversations = new ArrayList<>();

            controllerDebuggingMgmtService.clearAgentConversationRecords(userId, agentId, needDeleteConversations);

            // No assertions needed as the method should complete without errors
        });
    }

    @Test
    void test_clearAgentConversationRecords_no_stored_execs() {
        assertDoesNotThrow(() -> {
            String userId = "user1";
            String agentId = "agent1";
            List<ConversationInfo> needDeleteConversations = new ArrayList<>();
            ConversationInfo conversationInfo = mock(ConversationInfo.class);
            when(conversationInfo.getConversationId()).thenReturn("conv1");
            needDeleteConversations.add(conversationInfo);

            String execsKey = "key1";
            when(redisClient.get(eq(execsKey))).thenReturn("");

            controllerDebuggingMgmtService.clearAgentConversationRecords(userId, agentId, needDeleteConversations);

            // No assertions needed as the method should complete without errors
        });
    }

    @Test
    void test_clearAgentConversationRecords_with_stored_execs() {
        String userId = "user1";
        String agentId = "agent1";
        List<ConversationInfo> needDeleteConversations = new ArrayList<>();
        ConversationInfo conversationInfo = mock(ConversationInfo.class);
        when(conversationInfo.getConversationId()).thenReturn("conv1");
        needDeleteConversations.add(conversationInfo);

        String execsKey = "key1";
        when(redisClient.get(eq(execsKey))).thenReturn("[{\"id\":\"exec1\"}]");

        try (MockedStatic<JSON> jsonMock = mockStatic(JSON.class)) {
            List<ControllerExecutionBriefModel> execs = new ArrayList<>();
            execs.add(new ControllerExecutionBriefModel());
            jsonMock.when(() -> JSON.parseArray("[{\"id\":\"exec1\"}]", ControllerExecutionBriefModel.class))
                .thenReturn(execs);

            controllerDebuggingMgmtService.clearAgentConversationRecords(userId, agentId, needDeleteConversations);
        }
    }

    @Test
    void test_clearAgentConversationRecords_exception_handling() {
        assertDoesNotThrow(() -> {
            String userId = "user1";
            String agentId = "agent1";
            List<ConversationInfo> needDeleteConversations = new ArrayList<>();
            ConversationInfo conversationInfo = mock(ConversationInfo.class);
            when(conversationInfo.getConversationId()).thenReturn("conv1");
            needDeleteConversations.add(conversationInfo);

            String execsKey = "key1";
            when(redisClient.get(eq(execsKey))).thenThrow(new RuntimeException("Redis error"));

            controllerDebuggingMgmtService.clearAgentConversationRecords(userId, agentId, needDeleteConversations);

            // No assertions needed as the method should complete without errors
        });
    }

    @Test
    void test_clearAgentConversationRecords_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                List<ControllerExecutionBriefModel> storedCtrlExecs = new ArrayList<>();
                ControllerExecutionBriefModel controllerExecutionBriefModel = new ControllerExecutionBriefModel();
                storedCtrlExecs.add(controllerExecutionBriefModel);
                mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(ControllerExecutionBriefModel.class)))
                    .thenReturn(storedCtrlExecs);

                when(redisClient.get(anyString())).thenReturn("");
                when(redisClient.delete(anyString())).thenReturn(true);

                List<ConversationInfo> needDeleteConversations = new ArrayList<>();
                ConversationInfo conversationInfo = new ConversationInfo();
                needDeleteConversations.add(conversationInfo);

                // When
                controllerDebuggingMgmtService.clearAgentConversationRecords("not_empty", "not_empty",
                    needDeleteConversations);
            }
        });
    }

    @Test
    void test_clearAgentConversationRecords_should_not_throw_exception1() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                List<ControllerExecutionBriefModel> storedCtrlExecs = new ArrayList<>();
                ControllerExecutionBriefModel controllerExecutionBriefModel = new ControllerExecutionBriefModel();
                storedCtrlExecs.add(controllerExecutionBriefModel);
                mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(ControllerExecutionBriefModel.class)))
                    .thenReturn(storedCtrlExecs);

                when(redisClient.get(anyString())).thenReturn("");
                when(redisClient.delete(anyString())).thenReturn(true);

                List<ConversationInfo> needDeleteConversations = new ArrayList<>();
                ConversationInfo conversationInfo = new ConversationInfo();
                needDeleteConversations.add(conversationInfo);

                // When
                controllerDebuggingMgmtService.clearAgentConversationRecords("", "", needDeleteConversations);
            }
        });
    }

    @Test
    void test_clearAgentConversationRecords_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                List<ControllerExecutionBriefModel> storedCtrlExecs = new ArrayList<>();
                ControllerExecutionBriefModel controllerExecutionBriefModel = new ControllerExecutionBriefModel();
                storedCtrlExecs.add(controllerExecutionBriefModel);
                mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(ControllerExecutionBriefModel.class)))
                    .thenReturn(storedCtrlExecs);

                when(redisClient.get(anyString())).thenReturn("not_empty");
                when(redisClient.delete(anyString())).thenReturn(true);

                List<ConversationInfo> needDeleteConversations = new ArrayList<>();
                ConversationInfo conversationInfo = new ConversationInfo();
                needDeleteConversations.add(conversationInfo);

                // When
                controllerDebuggingMgmtService.clearAgentConversationRecords("not_empty", "not_empty",
                    needDeleteConversations);
            }
        });
    }

    @Test
    void test_clearAgentConversationRecords_should_not_throw_exception3() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(ControllerExecutionBriefModel.class)))
                    .thenReturn(null);

                when(redisClient.get(anyString())).thenReturn(null);
                when(redisClient.delete(anyString())).thenReturn(true);

                // When
                controllerDebuggingMgmtService.clearAgentConversationRecords(null, null, null);
            }
        });
    }

    @Test
    void test_clearAgentConversationRecords_should_not_throw_exception6() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                List<ControllerExecutionBriefModel> storedCtrlExecs = new ArrayList<>();
                ControllerExecutionBriefModel controllerExecutionBriefModel = new ControllerExecutionBriefModel();
                storedCtrlExecs.add(controllerExecutionBriefModel);
                mockedStaticJSON.when(() -> JSON.parseArray(anyString(), eq(ControllerExecutionBriefModel.class)))
                    .thenReturn(storedCtrlExecs);

                when(redisClient.get(anyString())).thenReturn("not_empty");
                doThrow(NullPointerException.class).when(redisClient).deleteByPrefix(anyString());
                when(redisClient.delete(anyString())).thenReturn(true);

                List<ConversationInfo> needDeleteConversations = new ArrayList<>();
                ConversationInfo conversationInfo = new ConversationInfo();
                needDeleteConversations.add(conversationInfo);

                // When
                controllerDebuggingMgmtService.clearAgentConversationRecords("not_empty", "not_empty",
                    needDeleteConversations);
            }
        });
    }

    @Test
    void test_clearAgentExecutionRecordsAsync() {
        when(redisClient.delete(anyString())).thenReturn(true);
        ControllerExecutionBriefModel model = new ControllerExecutionBriefModel();
        model.setExecutionId("exe");

        controllerDebuggingMgmtService.clearAgentExecutionRecordsAsync("agent", List.of(model)).join();
    }
}
