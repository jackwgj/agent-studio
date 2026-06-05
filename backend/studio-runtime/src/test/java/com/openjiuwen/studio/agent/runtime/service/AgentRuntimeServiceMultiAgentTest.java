/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.AppType.CONTROLLER;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_AGENT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_TOKEN;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_UPDATED_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.PublishUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunRsp;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.AgentIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * AgentRuntimeService测试类
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class AgentRuntimeServiceMultiAgentTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Autowired
    PublishUtils publishUtils;

    @Autowired
    private AgentRuntimeService agentRuntimeService;

    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentIrCacheService agentIrCacheService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentOpsService opsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsService obsService;

    @BeforeAll
    static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(TEST_TOKEN);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-cn");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(agentRuntimeService, "agentIrCacheService", agentIrCacheService);
        when(agentIrCacheService.getLatestAgentIr(anyString()).getMetadata()).thenReturn(mockAgentIrMetadata());

        ReflectionTestUtils.setField(agentRuntimeService, "obsService", obsService);
        ReflectionTestUtils.setField(agentRuntimeService, "opsService", opsService);
        when(obsService.getObject(anyString())).thenReturn("");
        when(obsService.getObject(anyString(), anyString())).thenReturn(new HashMap<>());
        doNothing().when(obsService).putObject(anyString(), anyString());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_runAgent_should_succeed_when_test_multi_agent() {
        AgentExecuteParams executeParams = new AgentExecuteParams();
        executeParams.setAgentId(TEST_AGENT_ID);
        executeParams.setConversationId(ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID);
        executeParams.setInputs(Map.of("query", "nihao"));
        executeParams.setProjectId(TEST_PROJECT_ID);
        executeParams.setExecuteType(CONTROLLER);
        executeParams.setDebug(true);
        executeParams.setType("DEBUG");
        executeParams.setWorkspaceId("default");
        executeParams.setOwnerWorkspaceId("default");
        executeParams.setApiLog(new ModelApiLog());

        // run test
        WorkflowRunRsp workflowRunRsp = (WorkflowRunRsp) agentRuntimeService.runAgent(executeParams);
        assertEquals(1, workflowRunRsp.getMessages().size());
        assertEquals(5, workflowRunRsp.getNodeInfo().size());
        assertEquals("Questioner", workflowRunRsp.getMessages().get(0).getNodeType());

        assertEquals(2, workflowRunRsp.getEvents().size());
        assertEquals("workflow_start", workflowRunRsp.getEvents().get(0).getEvent());
        assertEquals("workflow_blocked", workflowRunRsp.getEvents().get(1).getEvent());

        assertNull(workflowRunRsp.getErrorCode());
        assertNull(workflowRunRsp.getErrorMessage());
    }

    @Test
    void test_runAgent_without_history() {
        AgentExecuteParams executeParams = new AgentExecuteParams();
        executeParams.setAgentId(TEST_AGENT_ID);
        executeParams.setConversationId(ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID);
        executeParams.setInputs(Map.of("query", "nihao"));
        executeParams.setProjectId(TEST_PROJECT_ID);
        executeParams.setExecuteType(CONTROLLER);
        executeParams.setDebug(true);
        executeParams.setType("DEBUG");
        executeParams.setHistoryEnabled(false);

        // run test
        WorkflowRunRsp workflowRunRsp = (WorkflowRunRsp) agentRuntimeService.runAgent(executeParams);
        assertEquals(1, workflowRunRsp.getMessages().size());
    }

    @Test
    void test_runAgent_should_succeed_when_test_multi_agent_no_debug() {
        AgentExecuteParams executeParams = new AgentExecuteParams();
        executeParams.setAgentId(TEST_AGENT_ID);
        executeParams.setConversationId(ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID);
        executeParams.setInputs(Map.of("query", "nihao"));
        executeParams.setProjectId(TEST_PROJECT_ID);
        executeParams.setExecuteType(CONTROLLER);
        executeParams.setDebug(false);
        executeParams.setOwnerWorkspaceId("default");
        executeParams.setWorkspaceId("default");
        executeParams.setApiLog(new ModelApiLog());

        // run test
        WorkflowRunRsp workflowRunRsp = (WorkflowRunRsp) agentRuntimeService.runAgent(executeParams);
        assertEquals(1, workflowRunRsp.getMessages().size());
        assertEquals(0, workflowRunRsp.getNodeInfo().size());
        assertEquals("Questioner", workflowRunRsp.getMessages().get(0).getNodeType());
        assertNull(workflowRunRsp.getErrorCode());
        assertNull(workflowRunRsp.getErrorMessage());
    }

    @Test
    void test_runAgent_should_failed_when_test_multi_agent() {
        AgentExecuteParams executeParams = new AgentExecuteParams();
        executeParams.setAgentId(TEST_AGENT_ID);
        executeParams.setConversationId(ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID + "_failed");
        executeParams.setInputs(Map.of("query", "nihao"));
        executeParams.setProjectId(TEST_PROJECT_ID);
        executeParams.setExecuteType(CONTROLLER);
        executeParams.setWorkspaceId("default");
        executeParams.setOwnerWorkspaceId("default");
        executeParams.setApiLog(new ModelApiLog());

        // run test
        WorkflowRunRsp workflowRunRsp = (WorkflowRunRsp) agentRuntimeService.runAgent(executeParams);
        assertEquals(0, workflowRunRsp.getMessages().size());
        assertEquals(0, workflowRunRsp.getNodeInfo().size());
        assertEquals(StudioError.UNEXPECTED_ERROR.getFullCode(), workflowRunRsp.getErrorCode());
    }

    @Test
    void test_runAgent_should_failed_when_test_multi_agent_latest() {
        AgentExecuteParams executeParams = new AgentExecuteParams();
        executeParams.setAgentId(TEST_AGENT_ID);
        executeParams.setConversationId(ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID + "_failed");
        executeParams.setInputs(Map.of("query", "nihao"));
        executeParams.setProjectId(TEST_PROJECT_ID);
        executeParams.setExecuteType(CONTROLLER);
        executeParams.setLatestPublish(true);

        assertThrows(AgentStudioException.class, () -> agentRuntimeService.runAgent(executeParams));
    }

    @Test
    void test_runAgent_should_success_when_test_multi_agent_latest() {
        AgentExecuteParams executeParams = new AgentExecuteParams();
        executeParams.setAgentId(TEST_AGENT_ID);
        executeParams.setConversationId(ConstantTest.Conversation.TEST_MULTI_AGENT_CONVERSATION_ID);
        executeParams.setInputs(Map.of("query", "nihao"));
        executeParams.setProjectId(TEST_PROJECT_ID);
        executeParams.setExecuteType(CONTROLLER);
        executeParams.setLatestPublish(true);
        executeParams.setWorkspaceId("default");
        executeParams.setOwnerWorkspaceId("default");
        executeParams.setApiLog(new ModelApiLog());

        publishUtils.savePublish(AgentMetadata.builder().agentId(TEST_AGENT_ID).projectId(TEST_PROJECT_ID).build(),
            Constants.LATEST_PUBLISH_VERSION, obsService);

        WorkflowRunRsp workflowRunRsp = (WorkflowRunRsp) agentRuntimeService.runAgent(executeParams);
        assertEquals(1, workflowRunRsp.getMessages().size());
        assertEquals("Questioner", workflowRunRsp.getMessages().get(0).getNodeType());
        assertNull(workflowRunRsp.getErrorCode());
        assertNull(workflowRunRsp.getErrorMessage());
    }

    private AgentIrMetadata mockAgentIrMetadata() {
        AgentIrMetadata metadata = new AgentIrMetadata();
        metadata.setProjectId(TEST_PROJECT_ID);
        metadata.setUpdatedAt(TEST_UPDATED_TIME);
        return metadata;
    }
}
