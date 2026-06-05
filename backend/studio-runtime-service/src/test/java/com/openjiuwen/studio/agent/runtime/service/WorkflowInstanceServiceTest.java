/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.dto.agent.ConversationInfo;
import com.openjiuwen.studio.agent.common.dto.agent.ExecutionInfo;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrReq;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrRsp;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import java.util.concurrent.CompletableFuture;

@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowInstanceServiceTest {
    @Test
    public void copyTest() {
        WorkflowInstanceEntity source = new WorkflowInstanceEntity();
        WorkflowInstanceEntity target = new WorkflowInstanceEntity();

        WorkflowInstanceService service = new WorkflowInstanceService();
        service.copy(source, target);
        Assertions.assertNotNull(target.getEventList());

        target.setEventList(new ArrayList<>());
        service.copy(source, target);
        Assertions.assertNotNull(target.getEventList());

        target.setEventList(null);
        source.setEventList(new ArrayList<>());
        source.getEventList().add(new JiuwenEvent());

        service.copy(source, target);
        Assertions.assertNotNull(target.getEventList());
        Assertions.assertEquals(1, target.getEventList().size());

        target.setEventList(new ArrayList<>());
        target.getEventList().add(new JiuwenEvent());
        service.copy(source, target);
        Assertions.assertNotNull(target.getEventList());
        Assertions.assertEquals(2, target.getEventList().size());
    }

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JiuWenClient jiuWenClient;

    @InjectMocks
    private WorkflowInstanceService workflowInstanceService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(workflowInstanceService, "insightExecRel", "not_empty");
        ReflectionTestUtils.setField(workflowInstanceService, "insightConvRel", "not_empty");
        ReflectionTestUtils.setField(workflowInstanceService, "insightExec", "not_empty");
        ReflectionTestUtils.setField(workflowInstanceService, "instExpire", 0L);
        ReflectionTestUtils.setField(workflowInstanceService, "convExpire", 0L);
        ReflectionTestUtils.setField(workflowInstanceService, "maxExecutionSize", 0);
        ReflectionTestUtils.setField(workflowInstanceService, "maxConversationSize", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_clearWorkflowConversationRecords_normal() {
        String workflowId = "workflow1";
        String userId = "user1";
        String versionId = "version1";
        String token = "token1";

        ConversationInfo conversationInfo = mock(ConversationInfo.class);
        when(conversationInfo.getConversationId()).thenReturn("convId1");

        List<ConversationInfo> needDeleteInfos = List.of(conversationInfo);

        String executionInfoKey = "key1";

        String executionInfoJson = "[{\"executionId\":\"exec1\"}]";
        when(redisClient.get(eq(executionInfoKey))).thenReturn(executionInfoJson);

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setExecutionId("exeId");
        when(redisClient.get("not_empty_version1")).thenReturn(JsonUtils.encode(List.of(executionInfo)));

        CompletableFuture<Void> future = workflowInstanceService.clearWorkflowConversationRecords(workflowId, userId,
            versionId, needDeleteInfos, token);
        future.join();
    }

    @Test
    void test_clearWorkflowConversationRecords_exception() {
        String workflowId = "workflow1";
        String userId = "user1";
        String versionId = "version1";
        String token = "token1";

        ConversationInfo conversationInfo = mock(ConversationInfo.class);
        when(conversationInfo.getConversationId()).thenReturn("convId1");

        List<ConversationInfo> needDeleteInfos = List.of(conversationInfo);

        String executionInfoKey = "key1";

        String executionInfoJson = "[{\"executionId\":\"exec1\"}]";
        when(redisClient.get(eq(executionInfoKey))).thenReturn(executionInfoJson);

        CompletableFuture<Void> future = workflowInstanceService.clearWorkflowConversationRecords(workflowId, userId,
            versionId, needDeleteInfos, token);
        future.join();
    }

    @Test
    void test_clearWorkflowConversationRecords_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            List<ExecutionInfo> executionInfos = new ArrayList<>();
            ExecutionInfo executionInfo = new ExecutionInfo();
            executionInfos.add(executionInfo);

            when(redisClient.delete(anyString())).thenReturn(true);
            when(redisClient.get(anyString())).thenReturn("not_empty");

            JiuWenDeleteIrRsp jiuWenDeleteIrRsp = JiuWenDeleteIrRsp.builder().build();
            when(jiuWenClient.deleteIr(anyString(), any(JiuWenDeleteIrReq.class))).thenReturn(
                jiuWenDeleteIrRsp);

            List<ConversationInfo> needDeleteInfos = new ArrayList<>();
            ConversationInfo conversationInfo = new ConversationInfo();
            needDeleteInfos.add(conversationInfo);

            // When
            CompletableFuture<Void> future = workflowInstanceService.clearWorkflowConversationRecords("not_empty",
                "not_empty", "not_empty", needDeleteInfos, "not_empty");
            future.join();
        });
    }

    @Test
    void test_clearWorkflowConversationRecords_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {

            when(redisClient.delete(anyString())).thenReturn(true);
            when(redisClient.get(anyString())).thenReturn(null);

            when(jiuWenClient.deleteIr(anyString(), any(JiuWenDeleteIrReq.class))).thenReturn(null);

            List<ConversationInfo> needDeleteInfos = new ArrayList<>();
            ConversationInfo conversationInfo = new ConversationInfo();
            needDeleteInfos.add(conversationInfo);

            // When
            CompletableFuture<Void> future = workflowInstanceService.clearWorkflowConversationRecords(null, null, null,
                needDeleteInfos, "token");
            future.join();
        });
    }

    @Test
    void test_clearWorkflowConversationRecords_should_not_throw_exception5() throws Exception {
        assertDoesNotThrow(() -> {
            // Given

            when(redisClient.delete(anyString())).thenReturn(true);
            when(redisClient.get(anyString())).thenReturn("not_empty");

            when(jiuWenClient.deleteIr(anyString(), any(JiuWenDeleteIrReq.class))).thenThrow(
                NullPointerException.class);

            List<ConversationInfo> needDeleteInfos = new ArrayList<>();
            ConversationInfo conversationInfo = new ConversationInfo();
            needDeleteInfos.add(conversationInfo);

            // When
            CompletableFuture<Void> future = workflowInstanceService.clearWorkflowConversationRecords("not_empty",
                "not_empty", "not_empty", needDeleteInfos, "not_empty");
            future.join();
        });
    }

    @Test
    void test_deleteExecution(){
        when(redisClient.delete(anyString())).thenReturn(true);
        try (MockedStatic<RequestContextUtils> requestContextUtilsMock = mockStatic(RequestContextUtils.class)) {
            requestContextUtilsMock.when(RequestContextUtils::getRequestUserId).thenReturn("userId");

            WorkflowInstanceEntity entity = new WorkflowInstanceEntity();
            entity.setId("id");
            // Call the method under test
            workflowInstanceService.deleteExecution("testUserId", entity, "versionId");
        }
    }

    @Test
    void test_clearExecutionRecordsAsync(){
        when(redisClient.delete(anyString())).thenReturn(true);
        when(redisClient.get(anyString())).thenReturn("not_empty");

        when(jiuWenClient.deleteIr(anyString(), any(JiuWenDeleteIrReq.class))).thenThrow(
            NullPointerException.class);

        List<ExecutionInfo> needDeleteInfos = new ArrayList<>();

        // When
        CompletableFuture<Void> future = workflowInstanceService.clearExecutionRecordsAsync("not_empty",
            "not_empty", needDeleteInfos);
        future.join();
    }
}
