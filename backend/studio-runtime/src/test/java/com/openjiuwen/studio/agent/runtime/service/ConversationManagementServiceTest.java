/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.Conversation.TEST_CONVERSATION_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_AGENT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_AGENT_NAME;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_DOMAIN_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_EXECUTION_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_MESSAGE_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_MODEL_DEPLOYMENT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_TOKEN;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_USER_ID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.service.DelegateExchangeTokenService;
import com.openjiuwen.studio.agent.common.dto.AgentExecutionInfo;
import com.openjiuwen.studio.agent.common.dto.run.AgentExecutionQueries;
import com.openjiuwen.studio.agent.common.dto.AgentInvokeInfo;
import com.openjiuwen.studio.agent.common.dto.run.ConversationDeleteResp;
import com.openjiuwen.studio.agent.common.dto.agent.ConversionQueries;
import com.openjiuwen.studio.agent.common.dto.agent.Feedback;
import com.openjiuwen.studio.agent.common.dto.agent.FeedbackReason;
import com.openjiuwen.studio.agent.common.dto.run.GetAgentExecutionInfoQo;
import com.openjiuwen.studio.agent.runtime.dto.GetMemoryChunksQo;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenAgentEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenAgentEventData;
import com.openjiuwen.studio.agent.common.dto.run.ListAgentConversationsQo;
import com.openjiuwen.studio.agent.common.dto.run.ListAgentExecutionQueriesQo;
import com.openjiuwen.studio.agent.runtime.dto.MemoryChunk;
import com.openjiuwen.studio.agent.runtime.dto.MemoryVariable;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.common.dto.run.RetrieveConversationQo;
import com.openjiuwen.studio.agent.runtime.entity.FeedbackEntity;
import com.openjiuwen.studio.agent.runtime.entity.HyperParameters;
import com.openjiuwen.studio.agent.runtime.entity.ModelConfig;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.AgentIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.Conversation;
import com.openjiuwen.studio.agent.runtime.model.ConversationParams;
import com.openjiuwen.studio.agent.runtime.rce.model.CreateBackflowReq;
import com.openjiuwen.studio.agent.runtime.rce.service.DataManagerService;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.utils.TestUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assistant运行态Service测试类
 *
 */
@Slf4j
public class ConversationManagementServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Autowired
    ConversationManagementService conversationManagementService;

    @Autowired
    AgentRuntimeService agentRuntimeService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    DataManagerService dataManagerService;

    @MockitoBean
    RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DelegateExchangeTokenService delegateExchangeTokenService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentIrCacheService agentIrCacheService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisLock lock;

    private AutoCloseable mockitoCloseable;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(TEST_TOKEN, TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(TEST_USER_ID);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(conversationManagementService, "agentIrCacheService", agentIrCacheService);
        ReflectionTestUtils.setField(conversationManagementService, "dataManagerService", dataManagerService);
        ReflectionTestUtils.setField(agentRuntimeService, "agentIrCacheService", agentIrCacheService);
        ReflectionTestUtils.setField(conversationManagementService, "delegateExchangeTokenService",
                delegateExchangeTokenService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @SneakyThrows
    @Test
    void testConversationAction() {

        String projectId = TEST_PROJECT_ID;
        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        Message message = new Message().setRole("user").setContent("你好");
        messages.add(message);
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(projectId)
                .conversationId(conversationId)
                .build();

        when(redisClient.get("456_id_test_user_id")).thenReturn(
                "{\"messageList\":[{\"content\":\"你好\",\"role\":\"user\"}],\"lastUpdateTime\": 1234567890123}");

        when(redisClient.getLock(anyString())).thenReturn(mockLock());
        final List<Message> messagesAfterUpdate = conversationManagementService.updateConversation(conversationParams,
                messages, true);
        final List<Message> getMessages = conversationManagementService.retrieveConversation(projectId, "id",
                conversationId, new RetrieveConversationQo());
        getMessages.add(message);

        when(redisClient.get("insight_conv_id_test_user_id")).thenReturn(
                "[{\"conversation_id\":\"id\",\"start_time\":1234567890123}]");

        final ConversationDeleteResp unused = conversationManagementService.deleteConversation(projectId, "id",
                conversationId, null);
        JiuwenAgentEventData jiuwenAgentEventData = JSONObject.parseObject(
                TestUtil.getStringFromFile("classpath:response/agent_node_message_mcp.json"), JiuwenAgentEventData.class);
        AgentInvokeInfo agentInvokeInfo = conversationManagementService.convertAgentInvokeInfo(new AgentExecutionInfo(),
                jiuwenAgentEventData, new AgentExecuteParams());
        Assertions.assertNotNull(agentInvokeInfo);
        Assertions.assertEquals(unused.getId(), conversationId);
    }

    @SneakyThrows
    @Test
    void testAddConversation() {
        when(redisClient.exists("456_id_test_user_id")).thenReturn(false);
        when(redisClient.exists("insight_conv_id_test_user_id")).thenReturn(false);

        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message().setRole("user").setContent("你好"));
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(TEST_PROJECT_ID)
                .conversationId(conversationId)
                .build();
        conversationManagementService.updateConversation(conversationParams, messages, true);
    }

    @SneakyThrows
    @Test
    void testNoConversationRetrieve() {
        when(redisClient.exists("456_id_test_user_id")).thenReturn(false);
        String conversationId = "456";
        List<Message> messages = conversationManagementService.retrieveConversation(TEST_PROJECT_ID, "id",
                conversationId, new RetrieveConversationQo());
        Assertions.assertEquals(new ArrayList<>(), messages);
    }

    @SneakyThrows
    @Test
    void test_deleteConversation_should_throw_AgentRuntimeException_when_encounter_exception() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            // setup
            when(redisClient.delete(any())).thenThrow(RedisException.class);
            // run test
            conversationManagementService.deleteConversation(TEST_PROJECT_ID, "id", "456", null);
        });
    }

    @Test
    void test_retrieveConversationMemory_should_succeed_when_test_data_combination() {
        when(redisClient.get("test_agent_id_test_user_id")).thenReturn(
                "[{\"default_value\":\"000000\",\"description\":\"编号\",\"life_cycle\":\"user\",\"name\":\"id\",\"updated_on\":1732879408819,\"value\":\"123456\"}]");
        when(redisClient.get("test_agent_id_test-user-conversation-123")).thenReturn(
                "[{\"default_value\":\"张三\",\"description\":\"姓名\",\"life_cycle\":\"conversation\",\"name\":\"name\",\"updated_on\":1732879408819,\"value\":\"李四\"}]");
        when(agentIrCacheService.getLatestAgentIr(eq(TEST_AGENT_ID))).thenReturn(mockAgentIrWrapper());

        // run test
        List<MemoryVariable> memoryVariables = conversationManagementService.retrieveConversationMemory(TEST_PROJECT_ID,
                TEST_AGENT_ID, TEST_CONVERSATION_ID, null);

        // verify result
        Assertions.assertEquals(2, memoryVariables.size());
    }

    @Test
    void test_retrieveConversationMemory_should_throw_AgentRuntimeException_when_operate_redis_failed() {
        assertThrows(AgentStudioException.class, () -> {
            when(redisClient.get("test_agent_id_test_user_id")).thenThrow(RedisException.class);
            when(redisClient.exists(any())).thenReturn(true);

            // run test
            conversationManagementService.retrieveConversationMemory(TEST_PROJECT_ID, TEST_AGENT_ID,
                    TEST_CONVERSATION_ID, null);
        });
    }

    @Test
    void test_resetConversationMemory_should_succeed_when_test_data_combination() {
        when(agentIrCacheService.getLatestAgentIr(eq(TEST_AGENT_ID))).thenReturn(mockAgentIrWrapper());

        // run test
        List<MemoryVariable> memoryVariables = conversationManagementService.resetConversationMemory(TEST_PROJECT_ID,
                TEST_CONVERSATION_ID, TEST_AGENT_ID, null);

        // verify result
        Assertions.assertEquals(2, memoryVariables.size());
    }

    @Test
    void test_resetConversationMemory_should_throw_AgentRuntimeException_when_operate_redis_failed() {
        assertThrows(AgentStudioException.class, () -> {
            when(redisClient.delete(anyString())).thenThrow(RedisException.class);
            conversationManagementService.resetConversationMemory(TEST_PROJECT_ID, TEST_CONVERSATION_ID, TEST_AGENT_ID,
                    null);
        });
    }

    @Test
    void test_updateMemoryVariables_should_succeed_when_test_data_combination() {
        assertDoesNotThrow(() -> {
            conversationManagementService.updateUserMemoryVariables(TEST_AGENT_ID, TEST_USER_ID, null,
                    mockMemoryVariableList());
            conversationManagementService.updateConMemoryVariables(TEST_AGENT_ID, TEST_CONVERSATION_ID, null,
                    mockMemoryVariableList());
        });
    }

    @Test
    void test_updateMemoryVariables_should_throw_AgentRuntimeException_when_operate_redis_failed() {
        assertThrows(AgentStudioException.class, () -> {
            // set up
            doThrow(RedisException.class).when(redisClient).set(any(), any(), any());

            // run test
            conversationManagementService.updateUserMemoryVariables(TEST_AGENT_ID, TEST_USER_ID, null,
                    mockMemoryVariableList());
        });
    }

    @Test
    void test_saveAgentExecutionInfo() {
        assertThrows(AgentStudioException.class, () -> {
            conversationManagementService.saveAgentExecutionInfo(mockJiuwenAgentPluginEvent(), mockAgentExecuteParams(),
                    null);
            conversationManagementService.saveAgentExecutionInfo(mockJiuwenAgentChainEvent(), mockAgentExecuteParams(),
                    null);
        });
    }

    @Test
    void test_saveAgentExecutionInfo_should_throw_AgentRuntimeException_when_operate_redis_failed() {
        assertThrows(AgentStudioException.class, () -> {
            // set up
            doThrow(RedisException.class).when(redisClient).set(any(), any(), any());

            when(redisClient.getLock(anyString())).thenReturn(lock);

            // 可选：模拟 tryLock 有返回值的情况
            when(lock.tryLock(any())).thenReturn(true);

            // run test
            conversationManagementService.saveAgentExecutionInfo(mockJiuwenAgentPluginEvent(), mockAgentExecuteParams(),
                    null);
            conversationManagementService.saveAgentExecutionInfo(mockJiuwenAgentChainEvent(), mockAgentExecuteParams(),
                    null);
        });
    }

    @Test
    void test_listAgentConversations() {
        // set up
        when(redisClient.get(any())).thenReturn(
                "[{\"conversation_id\":\"ConstantTest.Conversation.TEST_CONVERSATION_ID\",\"start_time\":1735900534063}]");
        when(redisClient.exists(any())).thenReturn(true);

        // run test
        ConversionQueries conversionQueries = conversationManagementService.listAgentConversations(TEST_PROJECT_ID,
                TEST_AGENT_ID, new ListAgentConversationsQo());

        // verify result
        Assertions.assertEquals(1, conversionQueries.getConversationInfos().size());
    }

    @Test
    void test_listAgentExecutionQueries() {
        // set up
        when(redisClient.get(any())).thenReturn(
                "[{\"execution_id\":\"test_execution_id\",\"query\":\"你是谁\",\"start_time\":1735900534063}]");
        when(redisClient.exists(any())).thenReturn(true);

        // run test
        AgentExecutionQueries agentExecutionQueries = conversationManagementService.listAgentExecutionQueries(
                TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, new ListAgentExecutionQueriesQo());

        // verify result
        Assertions.assertEquals(1, agentExecutionQueries.getExecutionQueries().size());
    }

    @Test
    void test_getAgentExecutionInfo() {
        // set up
        when(redisClient.get(anyString())).thenReturn(JSONObject.toJSONString(mockAgentExecutionInfo()));
        when(redisClient.exists(any())).thenReturn(true);

        // run test
        AgentExecutionInfo agentExecutionInfo = conversationManagementService.getAgentExecutionInfo(TEST_PROJECT_ID,
                TEST_EXECUTION_ID, TEST_AGENT_ID, new GetAgentExecutionInfoQo());

        // verify result
        Assertions.assertEquals(TEST_EXECUTION_ID, agentExecutionInfo.getExecutionId());
    }

    @SneakyThrows
    @Test
    void testGenerateMemoryChunk() {
        when(redisClient.exists(any())).thenReturn(false);
        when(redisClient.get("memory_chunks_test_user_id_test_agent_id_test-user-conversation-123")).thenReturn(
                "{\"messageList\":[{\"content\":\"你好\",\"role\":\"user\", \"created_at\":\"1737441014460\"}],\"lastUpdateTime\": 1234567890123}");
        when(redisClient.get("test-user-conversation-123_test_agent_id_test_user_id")).thenReturn(
                "{\"messageList\":[{\"content\":\"你好\",\"role\":\"user\", \"created_at\":\"1737441014460\"}],\"lastUpdateTime\": 1234567890123}");
        when(agentIrCacheService.getLatestAgentIr(eq(TEST_AGENT_ID))).thenReturn(mockAgentIrWrapper());
        conversationManagementService.generateMemoryChunk(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, null,
                null, null, null, null, "");
    }

    @SneakyThrows
    @Test
    void testGenerateMemoryChunkWithNullCreatedAt() {
        when(redisClient.exists(any())).thenReturn(false);
        when(agentIrCacheService.getLatestAgentIr(eq(TEST_AGENT_ID))).thenReturn(mockAgentIrWrapper());
        MemoryChunk result = conversationManagementService.generateMemoryChunk(TEST_PROJECT_ID, TEST_AGENT_ID,
                TEST_CONVERSATION_ID, null, null, null, null, null, "");
        assertNotNull(result);
        assertNull(result.getStart());
        assertNull(result.getEnd());
    }

    @SneakyThrows
    @Test
    void testGetMemoryChunks() {
        when(redisClient.get("memory_chunks_test_user_id_test_agent_id_test-user-conversation-123")).thenReturn(
                "[{\"create_at\":\"2025-01-17 18:52:50.246\",\"id\":1,\"summary\":\"用户询问了关于实时交通状况的信息，但作为AI，我无法提供此类实时信息。用户可能需要使用导航软件或搜索引擎来查询实时的交通状况。虽然用户没有提供任何个人身份标识信息、位置信息或时间段信息，但这些信息在此次对话中并不是重点考察维度，所以不需要进行追问或记录。在未来的对话中，如果用户提供了相关信息，助手应该记录下来以提供更个性化的服务。\"}]");
        GetMemoryChunksQo getMemoryChunksQo = new GetMemoryChunksQo().setLimit(1);
        conversationManagementService.getMemoryChunks(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID,
                getMemoryChunksQo);
    }

    @SneakyThrows
    @Test
    void testDeleteMemoryChunks() {
        when(redisClient.get("memory_chunks_test_user_id_test_agent_id_test-user-conversation-123")).thenReturn(
                "[{\"create_at\":\"2025-01-17 18:52:50.246\",\"id\":1,\"summary\":\"用户询问了关于实时交通状况的信息，但作为AI，我无法提供此类实时信息。用户可能需要使用导航软件或搜索引擎来查询实时的交通状况。虽然用户没有提供任何个人身份标识信息、位置信息或时间段信息，但这些信息在此次对话中并不是重点考察维度，所以不需要进行追问或记录。在未来的对话中，如果用户提供了相关信息，助手应该记录下来以提供更个性化的服务。\"}]");
        conversationManagementService.deleteMemoryChunks(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, 1);
    }

    @SneakyThrows
    @Test
    void testDeleteMemoryChunksWithExistingChunks() {
        when(redisClient.exists(any())).thenReturn(true);
        when(redisClient.get("memory_chunks_test_user_id_test_agent_id_test-user-conversation-123")).thenReturn(
                "[{\"create_at\":\"2025-01-17 18:52:50.246\",\"id\":1,\"summary\":\"用户询问了关于实时交通状况的信息，但作为AI，我无法提供此类实时信息。用户可能需要使用导航软件或搜索引擎来查询实时的交通状况。虽然用户没有提供任何个人身份标识信息、位置信息或时间段信息，但这些信息在此次对话中并不是重点考察维度，所以不需要进行追问或记录。在未来的对话中，如果用户提供了相关信息，助手应该记录下来以提供更个性化的服务。\"}]");
        conversationManagementService.deleteMemoryChunks(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, 1);
    }

    @SneakyThrows
    @Test
    void testDeleteMemoryChunksWithNonExistingChunks() {
        when(redisClient.exists(any())).thenReturn(false);
        conversationManagementService.deleteMemoryChunks(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, 1);
    }

    @SneakyThrows
    @Test
    void testDeleteMemoryChunksWithAllChunks() {
        when(redisClient.exists(any())).thenReturn(true);
        when(redisClient.get("memory_chunks_test_user_id_test_agent_id_test-user-conversation-123")).thenReturn(
                "[{\"create_at\":\"2025-01-17 18:52:50.246\",\"id\":1,\"summary\":\"Chunk 1\"},"
                        + "{\"create_at\":\"2025-01-17 18:52:51.246\",\"id\":2,\"summary\":\"Chunk 2\"}]");
        conversationManagementService.deleteMemoryChunks(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, 0);
    }

    @SneakyThrows
    @Test
    void testDeleteMemoryChunksWithLargeChunkList() {
        when(redisClient.exists(any())).thenReturn(true);
        List<MemoryChunk> memoryChunks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            MemoryChunk chunk = new MemoryChunk();
            chunk.setId(i + 1);
            chunk.setSummary("Chunk " + i);
            memoryChunks.add(chunk);
        }
        when(redisClient.get(any())).thenReturn(JSON.toJSONString(memoryChunks));
        conversationManagementService.deleteMemoryChunks(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID, 50);
    }

    @SneakyThrows
    @Test
    void test_createUserFeedback() {
        // mock conversation response
        when(redisClient.exists(any())).thenReturn(true);
        String conversationKey = String.format("%s_%s_%s", TEST_CONVERSATION_ID, TEST_AGENT_ID,
                RequestContextUtils.getRequestUserId());
        when(redisClient.get(conversationKey)).thenReturn(mockConversation());

        // mock executor response
        String executorKey = String.format("insight_exec_%s_%s", RequestContextUtils.getRequestUserId(),
                TEST_EXECUTION_ID);
        when(redisClient.get(executorKey)).thenReturn(JSONObject.toJSONString(mockAgentExecutionInfo()));
        when(redisClient.getLock(anyString())).thenReturn(mockLock());

        conversationManagementService.createUserFeedback(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID,
                TEST_MESSAGE_ID.toString(), "agent", null, mockFeedback());
    }

    @SneakyThrows
    @Test
    void test_creatDataBackFlowJob() {
        // mock conversation response
        when(redisClient.exists(any())).thenReturn(true);
        String conversationKey = String.format("%s_%s_%s", TEST_CONVERSATION_ID, TEST_AGENT_ID,
                RequestContextUtils.getRequestUserId());
        when(redisClient.get(conversationKey)).thenReturn(mockConversation());

        // mock executor response
        String executorKey = String.format("insight_exec_%s_%s", RequestContextUtils.getRequestUserId(),
                TEST_EXECUTION_ID);
        when(redisClient.get(executorKey)).thenReturn(JSONObject.toJSONString(mockAgentExecutionInfo()));

        when(delegateExchangeTokenService.delegateExchangeToken(anyString(), anyString(), anyString())).thenReturn(
                "fake_token");
        doNothing().when(dataManagerService).createDataBackflowJob(anyString(), any(CreateBackflowReq.class));
        FeedbackEntity feedbackEntity = mockFeedbackEntity();
        conversationManagementService.creatDataBackflowJob(feedbackEntity);

        feedbackEntity.setAppType("workflow");
        when(redisClient.get(executorKey)).thenReturn(
                TestUtil.getStringFromFile("classpath:response/workflow_instance.json"));
        conversationManagementService.creatDataBackflowJob(feedbackEntity);
    }

    @SneakyThrows
    @Test
    void test_deleteUserFeedback() {
        // mock conversation response
        when(redisClient.exists(any())).thenReturn(true);
        String conversationKey = String.format("%s_%s_%s", TEST_CONVERSATION_ID, TEST_AGENT_ID,
                RequestContextUtils.getRequestUserId());
        when(redisClient.get(conversationKey)).thenReturn(mockConversation());

        when(redisClient.delete(any())).thenReturn(true);
        when(redisClient.getLock(anyString())).thenReturn(mockLock());
        conversationManagementService.deleteFeedback(TEST_PROJECT_ID, TEST_AGENT_ID, TEST_CONVERSATION_ID,
                TEST_MESSAGE_ID.toString(), null);
    }

    @Test
    void testUpdateConversationWithEmptyMessages() {
        String projectId = TEST_PROJECT_ID;
        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(projectId)
                .conversationId(conversationId)
                .build();

        List<Message> result = conversationManagementService.updateConversation(conversationParams, messages, true);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testUpdateConversationWithLargeMessages() {
        String projectId = TEST_PROJECT_ID;
        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            messages.add(new Message().setRole("user").setContent("消息" + i));
        }
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(projectId)
                .conversationId(conversationId)
                .build();

        List<Message> result = conversationManagementService.updateConversation(conversationParams, messages, true);
        Assertions.assertEquals(100, result.size());
    }

    @Test
    void testConcurrentUpdateAndDeleteConversation() throws InterruptedException {
        String projectId = TEST_PROJECT_ID;
        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message().setRole("user").setContent("你好"));
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(projectId)
                .conversationId(conversationId)
                .build();

        // 启动一个线程更新对话
        Thread updateThread = new Thread(() -> {
            conversationManagementService.updateConversation(conversationParams, messages, true);
        });

        // 启动另一个线程删除对话
        Thread deleteThread = new Thread(() -> {
            conversationManagementService.deleteConversation(projectId, "id", conversationId, null);
        });

        updateThread.start();
        deleteThread.start();

        updateThread.join();
        deleteThread.join();

        // 检查对话是否被正确删除
        List<Message> result = conversationManagementService.retrieveConversation(projectId, "id", conversationId,
                new RetrieveConversationQo());
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testResetConversationMemoryWhenMemoryInsufficient() {
        // 模拟内存不足的情况
        when(agentIrCacheService.getLatestAgentIr(eq(TEST_AGENT_ID))).thenThrow(OutOfMemoryError.class);

        assertThrows(OutOfMemoryError.class, () -> {
            conversationManagementService.resetConversationMemory(TEST_PROJECT_ID, TEST_CONVERSATION_ID, TEST_AGENT_ID,
                    null);
        });
    }

    @Test
    void testUpdateConversationWithOldDataFormat() {
        String projectId = TEST_PROJECT_ID;
        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        Message message = new Message().setRole("user").setContent("你好");
        messages.add(message);
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(projectId)
                .conversationId(conversationId)
                .build();

        // 模拟旧版本的数据格式
        when(redisClient.get("456_id_test_user_id")).thenReturn(
                "{\"messages\":[{\"content\":\"你好\",\"role\":\"user\"}],\"lastUpdateTime\": 1234567890123}");

        List<Message> result = conversationManagementService.updateConversation(conversationParams, messages, true);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void testPerformanceOfUpdateConversation() {
        String projectId = TEST_PROJECT_ID;
        String conversationId = "456";
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            messages.add(new Message().setRole("user").setContent("消息" + i));
        }
        ConversationParams conversationParams = ConversationParams.builder()
                .id("id")
                .projectId(projectId)
                .conversationId(conversationId)
                .build();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            conversationManagementService.updateConversation(conversationParams, messages, true);
        }
        long endTime = System.currentTimeMillis();

        // 假设期望的响应时间小于1000ms
        assertTrue(endTime - startTime < 1000);
    }

    private AgentIrWrapper mockAgentIrWrapper() {
        AgentIrWrapper agentIrWrapper = new AgentIrWrapper();
        AgentIrMetadata metadata = new AgentIrMetadata();
        metadata.setMemoryVariables(mockMemoryVariableList());
        agentIrWrapper.setMetadata(metadata);
        HyperParameters hyperParameters = new HyperParameters();
        hyperParameters.setTemperature(0.5);
        hyperParameters.setTopP(2.0);
        ModelConfig modelConfig = ModelConfig.builder()
                .modelName("test_model_name")
                .modelType("test_model_type")
                .hyperParameters(hyperParameters)
                .build();
        agentIrWrapper.setModelConfig(modelConfig);
        return agentIrWrapper;
    }

    private List<MemoryVariable> mockMemoryVariableList() {
        List<MemoryVariable> memoryVariables = new ArrayList<>();
        MemoryVariable memoryVariable0 = new MemoryVariable();
        memoryVariable0.setName("name");
        memoryVariable0.setDescription("姓名");
        memoryVariable0.setDefaultValue("张三");
        memoryVariable0.setLifeCycle("conversation");
        memoryVariables.add(memoryVariable0);
        MemoryVariable memoryVariable1 = new MemoryVariable();
        memoryVariable1.setName("id");
        memoryVariable1.setDescription("编号");
        memoryVariable1.setDefaultValue("000000");
        memoryVariable1.setLifeCycle("user");
        memoryVariables.add(memoryVariable1);
        return memoryVariables;
    }

    private JiuwenAgentEvent mockJiuwenAgentChainEvent() {
        JiuwenAgentEvent jiuwenAgentEvent = new JiuwenAgentEvent();
        JiuwenAgentEventData agentEventData = new JiuwenAgentEventData();
        agentEventData.setChainId(TEST_EXECUTION_ID);
        agentEventData.setInvokeType("chain");
        agentEventData.setOutputs("我是一个智能助手");
        agentEventData.setStartTime("2025-01-03T18:41:31.517645");
        agentEventData.setEndTime("2025-01-03T18:41:33.542586");
        jiuwenAgentEvent.setData(agentEventData);
        return jiuwenAgentEvent;
    }

    private JiuwenAgentEvent mockJiuwenAgentPluginEvent() {
        JiuwenAgentEvent jiuwenAgentEvent = new JiuwenAgentEvent();
        JiuwenAgentEventData agentEventData = new JiuwenAgentEventData();
        agentEventData.setChainId(TEST_EXECUTION_ID);
        agentEventData.setInvokeType("plugin");
        agentEventData.setOutputs("我是一个智能助手");
        agentEventData.setStartTime("2025-01-03T18:41:31.517645");
        agentEventData.setEndTime("2025-01-03T18:41:33.542586");
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("instance_attributes", "{\"plugin_name\":\"retrieval\"}");
        agentEventData.setMetaData(metaData);
        jiuwenAgentEvent.setData(agentEventData);
        return jiuwenAgentEvent;
    }

    private AgentExecutionInfo mockAgentExecutionInfo() {
        AgentExecutionInfo agentExecutionInfo = new AgentExecutionInfo();
        agentExecutionInfo.setExecutionId(TEST_EXECUTION_ID);
        agentExecutionInfo.setInputs("你是谁");
        agentExecutionInfo.setOutputs("我是一个智能助手");
        agentExecutionInfo.setStatus("finished");
        agentExecutionInfo.setStartTime(Long.valueOf("1735900534063"));
        agentExecutionInfo.setEndTime(Long.valueOf("1735900537306"));

        // 构建invoke list
        AgentInvokeInfo invokeInfo = new AgentInvokeInfo();
        invokeInfo.setNodeId("1");
        invokeInfo.setNodeName("节点1");
        invokeInfo.setNodeType("llm");
        invokeInfo.setNodeStatus("finished");
        invokeInfo.setStartTime(Long.valueOf("1735900534063"));
        invokeInfo.setEndTime(Long.valueOf("1735900537306"));
        invokeInfo.setInputs("{\"content\":\"你好\",\"role\":\"user\"}");
        invokeInfo.setOutputs("{\"content\":\"我是一个智能助手\",\"role\":\"assistant\"}");
        invokeInfo.setModelDeploymentId(TEST_MODEL_DEPLOYMENT_ID);
        List<AgentInvokeInfo> invokeInfoList = new ArrayList<>();
        invokeInfoList.add(invokeInfo);
        agentExecutionInfo.setInvokeList(invokeInfoList);
        return agentExecutionInfo;
    }

    private AgentExecuteParams mockAgentExecuteParams() {
        AgentExecuteParams agentExecuteParams = new AgentExecuteParams();
        agentExecuteParams.setConversationId(TEST_CONVERSATION_ID);
        agentExecuteParams.setQuery("你是谁");
        agentExecuteParams.setUploadOpsSwitch(true);
        return agentExecuteParams;
    }

    private Feedback mockFeedback() {
        Feedback feedback = new Feedback();
        feedback.setAppName(TEST_AGENT_NAME);
        feedback.setRating(-1);
        List<String> tags = new ArrayList<>();
        tags.add("内容不符");
        feedback.setReason(new FeedbackReason().setTags(tags).setContent("不好"));
        return feedback;
    }

    private FeedbackEntity mockFeedbackEntity() {
        FeedbackEntity feedbackEntity = new FeedbackEntity();
        feedbackEntity.setAppId(TEST_AGENT_ID);
        feedbackEntity.setAppType("agent");
        feedbackEntity.setAppName(TEST_AGENT_NAME);
        feedbackEntity.setDomainId(TEST_DOMAIN_ID);
        feedbackEntity.setProjectId(TEST_PROJECT_ID);
        feedbackEntity.setUserId(TEST_USER_ID);
        feedbackEntity.setVersionId(null);
        feedbackEntity.setConversationId(TEST_CONVERSATION_ID);
        feedbackEntity.setMessageId(TEST_MESSAGE_ID.toString());
        feedbackEntity.setFeedback(new Feedback().setRating(1));
        feedbackEntity.setCreateAt(System.currentTimeMillis());
        return feedbackEntity;
    }

    private String mockConversation() {
        List<Message> messages = new ArrayList<>();
        Message userMessage = new Message().setRole("user").setContent("你是谁").setCreateTime(1750171051020L);
        Message assistantMessage = new Message().setRole("assistant")
                .setContent("我是一个智能助手")
                .setCreateTime(TEST_MESSAGE_ID)
                .setExecutionId(TEST_EXECUTION_ID);
        messages.add(userMessage);
        messages.add(assistantMessage);
        Conversation conversation = new Conversation();
        conversation.setMessageList(messages);
        return JSONObject.toJSONString(conversation);
    }

    private RedisLock mockLock() {
        return new RedisLock() {
            @Override
            public boolean tryLock(Duration maxWait) {
                return true;
            }

            @Override
            public void unlock() {
            }
        };
    }

    @Test
    public void saveAgentExecutionToOpsTest() {
        JiuwenAgentEvent event = new JiuwenAgentEvent();
        event.setData(new JiuwenAgentEventData().setAnswer("answer"));

        AgentExecuteParams params = new AgentExecuteParams();
        conversationManagementService.saveAgentExecutionToOps(event, params);
        params.setUploadOpsSwitch(true);

        conversationManagementService.saveAgentExecutionToOps(event, params);
    }
}
