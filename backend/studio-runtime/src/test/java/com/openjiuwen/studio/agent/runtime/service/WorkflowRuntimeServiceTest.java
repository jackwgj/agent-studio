/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.Jiuwen.END_NODE_DEFAULT_OUTPUT_FIELD;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_USER_ID;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfo;
import com.openjiuwen.studio.agent.common.dto.agent.Status;
import com.openjiuwen.studio.agent.common.dto.run.ListConversationQueriesQo;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.PublishUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.common.dto.run.GetExecutionInsightQo;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;
import com.openjiuwen.studio.agent.common.dto.run.ListExecutionQueriesQo;
import com.openjiuwen.studio.agent.common.dto.run.PluginConfig;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunInfo;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunRsp;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;
import com.openjiuwen.studio.agent.runtime.enums.MessageRole;
import com.openjiuwen.studio.agent.runtime.model.Conversation;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.WorkflowRunResult;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrRsp;
import com.openjiuwen.studio.agent.runtime.rce.service.JiuWenService;
import com.openjiuwen.studio.agent.runtime.sensitive.SensitiveTrie;
import com.openjiuwen.studio.agent.runtime.sensitive.SensitiveTrieUtils;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;
import com.openjiuwen.studio.agent.runtime.utils.PerformUtils;
import com.openjiuwen.studio.agent.runtime.utils.TestUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

/**
 * 工作流运行接口
 *
 */
@Slf4j
public class WorkflowRuntimeServiceTest extends BaseTest {
    @Autowired
    PublishUtils publishUtils;

    @Autowired
    private WorkflowRuntimeService workflowRuntimeService;

    @MockitoBean
    private ObsService obsService;

    @Resource(name = "JiuwenWorkflowRunCoreService")
    private IWorkflowRunCoreService workflowRunCoreService;

    @Mock
    private SseEmitter sseEmitter;

    @Mock
    private OkHttpUtils httpUtils;

    @MockitoBean
    private JiuWenService jiuWenService;

    @Autowired
    private WorkflowIrCacheService workflowIrCacheService;

    @BeforeAll
    public static void before() {
        RequestContextUtils.setRequestAuthTokenAndUserId(ConstantTest.TEST_TOKEN, ConstantTest.TEST_PROJECT_ID,
            ConstantTest.TEST_USER_ID);
    }

    private List<PluginConfig> buildPluginParams() {
        // 构造plugin参数
        List<PluginConfig> pluginConfigs = new ArrayList<>();
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfigs.add(pluginConfig);
        pluginConfig.setPluginId(ConstantTest.TEST_PLUGIN_ID);
        Map<String, Object> config = new HashMap<>();
        config.put("name", "11");
        pluginConfig.setConfig(config);
        return pluginConfigs;
    }

    @SneakyThrows
    @Test
    void testRuntimeInputLengthExceed() {

        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);

        // mock uuid
        String uuid = "125bb1fd-1a74-48a2-97a9-6416cc603189";
        UUID instId = UUID.fromString(uuid);

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            char[] a = new char[100001];
            Arrays.fill(a, 'a');
            String mockStr = new String(a);
            inputs.put("query", mockStr);
            inputs.put("name", "test");
            Assertions.assertThrows(AgentStudioException.class, () -> {
                workflowRuntimeService.runWorkflow(ExecuteParams.builder()
                    .projectId(ConstantTest.TEST_PROJECT_ID)
                    .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                    .debug(false)
                    .stream(true)
                    .inputs(inputs)
                    .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                    .pluginConfigs(buildPluginParams())
                    .build());
            });
        }
    }

    @SneakyThrows
    @Test
    void testRuntimeService() {

        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);

        // mock uuid
        String uuid = "125bb1fd-1a74-48a2-97a9-6416cc603189";
        UUID instId = UUID.fromString(uuid);

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            inputs.put("name", "test");

            workflowRuntimeService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(false)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .apiLog(new ModelApiLog())
                .build());
        }
    }

    @SneakyThrows
    @Test
    void testReleaseRuntimeService() {

        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);
        publishUtils.savePublish(AgentMetadata.builder()
            .agentId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
            .projectId(TEST_PROJECT_ID)
            .build(), Constants.TREASURE_PUBLISH_VERSION, obsService);
        // mock uuid
        String uuid = "125bb1fd-1a74-48a2-97a9-6416cc603189";
        UUID instId = UUID.fromString(uuid);

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            inputs.put("name", "test");

            workflowRuntimeService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(false)
                .stream(true)
                .isTreasure(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .apiLog(new ModelApiLog())
                .build());
        }
    }

    @SneakyThrows
    @Test
    void testComponentExecuteService() {

        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);

        // mock uuid
        String uuid = "125bb1fd-1a74-48a2-97a9-6416cc603189";
        UUID instId = UUID.fromString(uuid);

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "写一首诗");

            workflowRuntimeService.runWorkflowStream(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(true)
                .isNodeExecute(true)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                .pluginConfigs(buildPluginParams())
                .build());
        }
    }

    @SneakyThrows
    @Test
    void testComponentFailed() {

        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);

        // mock uuid
        String uuid = "125bb1fd-1a74-48a2-97a9-6416cc603189";
        UUID instId = UUID.fromString(uuid);

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");

            workflowRuntimeService.runWorkflowStream(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(true)
                .isNodeExecute(true)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID)
                .pluginConfigs(buildPluginParams())
                .build());
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "");
        }
    }

    @SneakyThrows
    @Test
    void testRuntimeInsightService() {
        List<String> messgeList = new ArrayList<>();

        mock(messgeList);

        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("test", "test");
            workflowRunCoreService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(true)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID)
                .httpHeaders(httpHeaders)
                .sseEmitter(sseEmitter)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .apiLog(new ModelApiLog())
                .build(), new Conversation());
            GetExecutionInsightQo getExecutionInsightQo = new GetExecutionInsightQo();
            getExecutionInsightQo.setLimit(10);
            getExecutionInsightQo.setOffset(0);
            workflowRuntimeService.getExecutionInsight(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID,
                getExecutionInsightQo);

            ListConversationQueriesQo conversationQueriesQo = new ListConversationQueriesQo();
            conversationQueriesQo.setLimit(10);
            conversationQueriesQo.setOffset(0);
            workflowRuntimeService.listConversationQueries(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, conversationQueriesQo);
            ListExecutionQueriesQo executionQueriesQo = new ListExecutionQueriesQo();
            executionQueriesQo.setLimit(10);
            executionQueriesQo.setOffset(0);
            executionQueriesQo.setStartTime(0L);
            workflowRuntimeService.listExecutionQueries(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID,
                executionQueriesQo);
        }
    }

    @SneakyThrows
    @Test
    void testRuntimeInsightServiceWithVersion() {
        List<String> messgeList = new ArrayList<>();

        mock(messgeList);

        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("test", "test");
            workflowRunCoreService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(true)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID)
                .httpHeaders(httpHeaders)
                .sseEmitter(sseEmitter)
                .pluginConfigs(buildPluginParams())
                .releasedVersion("test_version_id")
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .apiLog(new ModelApiLog())
                .build(), new Conversation());
            GetExecutionInsightQo getExecutionInsightQo = new GetExecutionInsightQo();
            getExecutionInsightQo.setLimit(10);
            getExecutionInsightQo.setOffset(0);
            getExecutionInsightQo.setVersion("test_version_id");
            workflowRuntimeService.getExecutionInsight(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID,
                getExecutionInsightQo);

            ListConversationQueriesQo conversationQueriesQo = new ListConversationQueriesQo();
            conversationQueriesQo.setLimit(10);
            conversationQueriesQo.setOffset(0);
            conversationQueriesQo.setVersion("test_version_id");
            workflowRuntimeService.listConversationQueries(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, conversationQueriesQo);
            ListExecutionQueriesQo executionQueriesQo = new ListExecutionQueriesQo();
            executionQueriesQo.setLimit(10);
            executionQueriesQo.setOffset(0);
            executionQueriesQo.setStartTime(0L);
            executionQueriesQo.setVersion("test_version_id");
            workflowRuntimeService.listExecutionQueries(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, ConstantTest.Conversation.TEST_INSIGHT_CONVERSATION_ID,
                executionQueriesQo);
        }
    }

    @SneakyThrows
    @Test
    void testRuntimeServiceWorkflowNotExist() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            // mock obs服務
            when(obsService.getObject("fake_workflow_path", ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)).thenReturn(
                new HashMap<>());
            String uuid = "125bb1fd-1a74-48a2-97a9-6416cc603184";
            UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");

            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(instId);
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("query", "你好");
                workflowRuntimeService.runWorkflow(ExecuteParams.builder()
                    .projectId(ConstantTest.TEST_PROJECT_ID)
                    .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                    .debug(false)
                    .stream(false)
                    .inputs(inputs)
                    .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                    .pluginConfigs(buildPluginParams())
                    .build());
            }
        });
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testInvokeJiuwenServiceStream() {
        List<String> messgeList = new ArrayList<>();

        mock(messgeList);

        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("test", "test");
            ExecuteParams executeParams = ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(true)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                .httpHeaders(httpHeaders)
                .sseEmitter(sseEmitter)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .build();
            PerformUtils.accumulation(executeParams, Constant.Performance.LOAD_IR, 100L);
            workflowRunCoreService.runWorkflow(executeParams, new Conversation());
        }
    }

    @SneakyThrows
    @Test
    void testInvokeJiuwenServiceStreamFailed() {
        List<String> messgeList = new ArrayList<>();

        mock(messgeList);

        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");

        await().atMost(Duration.ofSeconds(1)).until(() -> {
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(instId);
                MockedStatic<RequestContextUtils> mockRequestContext = Mockito.mockStatic(RequestContextUtils.class);
                mockRequestContext.when(RequestContextUtils::getRequestUserId).thenReturn(TEST_USER_ID);
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("query", "你好");
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("test", "test");
                return workflowRunCoreService.runWorkflow(ExecuteParams.builder()
                    .projectId(ConstantTest.TEST_PROJECT_ID)
                    .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                    .debug(true)
                    .stream(true)
                    .inputs(inputs)
                    .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID_FAILED)
                    .httpHeaders(httpHeaders)
                    .sseEmitter(sseEmitter)
                    .pluginConfigs(buildPluginParams())
                    .inputsUserId(ConstantTest.TEST_USER_ID)
                    .apiLog(new ModelApiLog())
                    .startTime(System.currentTimeMillis())
                    .build(), new Conversation()) != null;
            }
        });
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testInvokeJiuwenServiceStreamAbort() {
        List<String> messgeList = new ArrayList<>();

        mock(messgeList, false);

        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");
        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            doThrow(new IOException()).when(sseEmitter).send(any(Object.class), any(MediaType.class));
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("test", "test");
            workflowRunCoreService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(true)
                .stream(true)
                .inputs(inputs)
                .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                .httpHeaders(httpHeaders)
                .sseEmitter(sseEmitter)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .apiLog(new ModelApiLog())
                .build(), new Conversation());
        }

        Thread.sleep(5 * 1000);
        Assertions.assertEquals(0, messgeList.size());
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testInvokeJiuwenService() {
        List<String> messgeList = new ArrayList<>();
        mock(messgeList);
        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            List<Message> messageList = new ArrayList<>();
            messageList.add(new Message().setRole(MessageRole.USER.name().toLowerCase(Locale.ROOT))
                .setContent("你好")
                .setCreateTime(System.currentTimeMillis()));
            WorkflowRunResult result = workflowRunCoreService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
                .debug(false)
                .stream(false)
                .inputs(inputs)
                .messages(messageList)
                .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
                .sseEmitter(sseEmitter)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .apiLog(new ModelApiLog())
                .startTime(System.currentTimeMillis())
                .build(), new Conversation());

            result.getWorkflowRunInfo().setStartTime(1L).setEndTime(1L);
            result.getInstance().setStartTime(1L);
            result.getInstance().setEndTime(1L);
            // Assertions
            // .assertEquals(
            // JSONObject.toJSONString(
            // JSONObject
            // .parseObject(TestUtil.getStringFromFile("classpath:service/workflow_run_result.json")),
            // JSONWriter.Feature.PrettyFormat),
            // JSONObject.toJSONString(result, JSONWriter.Feature.PrettyFormat));
        }
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testTwqInvokeJiuwenService() {
        List<String> messgeList = new ArrayList<>();
        mock(messgeList);
        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");

        try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
            mockedStatic.when(UUID::randomUUID).thenReturn(instId);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", "你好");
            WorkflowRunResult result = workflowRunCoreService.runWorkflow(ExecuteParams.builder()
                .projectId(ConstantTest.TEST_PROJECT_ID)
                .workspaceId("default")
                .workflowId(ConstantTest.Workflow.TEST_TWQ_WORKFLOW_ID)
                .debug(false)
                .stream(false)
                .inputs(inputs)
                .messages(new ArrayList<>())
                .conversationId(ConstantTest.Conversation.TEST_TWQ_CONVERSATION_ID)
                .sseEmitter(sseEmitter)
                .pluginConfigs(buildPluginParams())
                .inputsUserId(ConstantTest.TEST_USER_ID)
                .startTime(System.currentTimeMillis())
                .build(), new Conversation());

            result.getWorkflowRunInfo().setStartTime(1L).setEndTime(1L);
            result.getInstance().setStartTime(1L);
            result.getInstance().setEndTime(1L);
            result.getInstance().getEventList().forEach(event -> {
                event.setCreatedTime(1L);
                event.getData().setStartTime("2025-01-18T10:41:29.562080+08:00");
                event.getData().setEndTime("2025-01-18T10:41:29.562818+08:00");
            });
            Assertions.assertEquals(
                JSONObject.parseObject(TestUtil.getStringFromFile("classpath:service/workflow_twq_run_result.json"))
                    .toJSONString(JSONWriter.Feature.PrettyFormat),
                JSONObject.toJSONString(result, JSONWriter.Feature.PrettyFormat));
        }
    }

    private void mock(List<String> messgeList) throws IOException {
        mock(messgeList, true);
    }

    private void mock(List<String> messgeList, boolean isMockSse) throws IOException {
        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString())).thenReturn(
            TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);

        // mock sseEmitter send
        if (isMockSse) {
            doAnswer(invocationOnMock -> {
                WorkflowRunStreamRsp resp = invocationOnMock.getArgument(0);
                String msg = JSONObject.toJSONString(resp);
                messgeList.add(msg);
                log.info(msg);
                return null;
            }).when(sseEmitter).send(any(Object.class), any(MediaType.class));
        }
    }

    @SneakyThrows
    @Test
    void testAbortConversation() {
        when(jiuWenService.deleteConversationIr(anyString())).thenReturn(new JiuWenDeleteIrRsp());
        List<String> messgeList = new ArrayList<>();
        mock(messgeList);
        // mock uuid
        UUID instId = UUID.fromString("125bb1fd-1a74-48a2-97a9-6416cc603184");
        String projectId = "1234";
        String workflowId = "456";
        String conversationId = "test-user-conversation-123";
        Status result = workflowRuntimeService.abortConversation(projectId, workflowId, conversationId);

        Assertions.assertEquals(0, result.getCode());
        Assertions.assertEquals("succeeded", result.getDesc());
    }

    @Test
    void testConvertData() {
        JiuwenEventData jiuwenEventData = new JiuwenEventData();
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("requestStartTime", "1970-01-01T00:00:00.000000+00:00");
        outputs.put("firstTokenTime", "2025-01-01T00:00:00.000000+08:00");
        jiuwenEventData.setOutputs(outputs);
        jiuwenEventData.setStatus(JiuwenEventData.StatusEnum.FINISH);
        NodeRunInfo nodeRunInfo = JiuwenEventProcessor.convertNodeRunInfo(jiuwenEventData);
        Assertions.assertEquals(nodeRunInfo.getStartupTime(), 0);
    }

    @Test
    void testWorkflowIrCacheFailed() {
        ExecuteParams executeParams = ExecuteParams.builder().build();
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:service/workflow_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);
        when(obsService.getMd5(anyString(), anyString())).thenThrow(AgentStudioException.class);
        workflowIrCacheService.getWorkflowIr(null, null);
        Assertions.assertThrows(AgentStudioException.class,
            () -> workflowIrCacheService.refreshWorkflowIr(executeParams, null));
    }

    @Test
    void testWorkflowSensitiveMatch() {
        // mock obs服務
        when(obsService.getMd5(anyString(), anyString())).thenReturn(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        Map<String, String> workflowIr = new HashMap<>();
        workflowIr.put(Constant.Obs.MD5, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID);
        workflowIr.put(Constant.Obs.CONTENT,
            TestUtil.getStringFromFile("classpath:service/workflow_ir_with_sensitive.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(workflowIr);

        // 输入兜底匹配，流式
        ExecuteParams executeParams = ExecuteParams.builder()
            .projectId(ConstantTest.TEST_PROJECT_ID)
            .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
            .stream(true)
            .inputs(Map.of("query", "兜底匹配"))
            .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
            .inputsUserId(ConstantTest.TEST_USER_ID)
            .build();
        workflowRuntimeService.runWorkflowStream(executeParams);

        // 输入兜底匹配，非流式
        executeParams = ExecuteParams.builder()
            .projectId(ConstantTest.TEST_PROJECT_ID)
            .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
            .stream(false)
            .inputs(Map.of("query", "兜底匹配"))
            .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
            .inputsUserId(ConstantTest.TEST_USER_ID)
            .build();
        WorkflowRunRsp runRsp = workflowRuntimeService.runWorkflow(executeParams);
        Assertions.assertEquals(runRsp.getOutputs().get(END_NODE_DEFAULT_OUTPUT_FIELD), "我是输入兜底回复内容");

        // 输出匹配，非流式
        executeParams = ExecuteParams.builder()
            .projectId(ConstantTest.TEST_PROJECT_ID)
            .workflowId(ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID)
            .stream(false)
            .workspaceId("default")
            .inputs(Map.of("query", "天选"))
            .conversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID)
            .inputsUserId(ConstantTest.TEST_USER_ID)
            .startTime(System.currentTimeMillis())
            .apiLog(new ModelApiLog())
            .build();
        runRsp = workflowRuntimeService.runWorkflow(executeParams);
        // 原文：你好！有什么我可以帮助你的 吗 ？ 匹配规则：你好->【您好】，帮助->""
        Assertions.assertEquals(runRsp.getOutputs().get(END_NODE_DEFAULT_OUTPUT_FIELD).toString(),
            "【您好】！有什么我可以你的 吗 ?");
    }

    @Test
    void testProcessStreamMessage() {
        SensitiveTrie outputTrie = new SensitiveTrie();
        outputTrie.insert("打工人", SensitiveTrie.SensitiveOp.REPLACE, "xxx");
        SensitiveTrieUtils sensitiveTrieUtils = new SensitiveTrieUtils("agent",
            AgentMetadata.builder().isContentReviewEnable(false).build());
        ReflectionTestUtils.setField(sensitiveTrieUtils, "outputMixedTrie", outputTrie);
        ReflectionTestUtils.setField(sensitiveTrieUtils, "isContentReviewEnable", true);
        JiuwenEventProcessor eventProcessor = new JiuwenEventProcessor();
        WorkflowIrCacheService cacheService = Mockito.mock(WorkflowIrCacheService.class);
        ReflectionTestUtils.setField(eventProcessor, "workflowIrService", cacheService);
        when(cacheService.getWorkflowIr(any(), any())).thenReturn(new WorkflowIrWrapper());

        ExecuteParams executeParams = ExecuteParams.builder()
            .sensitiveTrieUtils(sensitiveTrieUtils)
            .stream(true)
            .sseEmitter(new SseEmitter())
            .build();
        JiuwenEvent event = new JiuwenEvent().setIndex(0).setCreatedTime(System.currentTimeMillis());
        SensitiveTrieUtils.MatchResultWrapper resultWrapper;

        JiuwenEventData eventData = new JiuwenEventData().setNodeId("id").setNodeType("end").setNodeName("end");
        event.setData(eventData);

        // think 匹配
        eventData.setThink("我是打");
        resultWrapper = eventProcessor.processSensitiveMatch(executeParams, event, false);
        Assertions.assertEquals(resultWrapper.getOp(), 0);
        Assertions.assertEquals(resultWrapper.getText(), "我是");
        // think 结束
        eventData.setThink("");
        resultWrapper = eventProcessor.processSensitiveMatch(executeParams, event, false);
        Assertions.assertEquals(resultWrapper.getOp(), SensitiveTrie.SensitiveOp.APPEND.getCode());
        Assertions.assertEquals(resultWrapper.getText(), "打");
        // output 匹配
        eventData.setAnswer("工人打工人");
        resultWrapper = eventProcessor.processSensitiveMatch(executeParams, event, false);
        Assertions.assertEquals(resultWrapper.getOp(), SensitiveTrie.SensitiveOp.REPLACE.getCode());
        Assertions.assertEquals(resultWrapper.getText(), "工人xxx");
    }

    @Test
    void testProcessCancelRequest() {
        JiuwenEvent event = new JiuwenEvent().setIndex(0).setCreatedTime(System.currentTimeMillis());
        JiuwenEventData eventData = new JiuwenEventData().setNodeId("id").setNodeType("end").setNodeName("end");
        event.setData(eventData);

        WorkflowRunResult result = new WorkflowRunResult();
        result.setWorkflowRunInfo(new WorkflowRunInfo());

        JiuwenEventProcessor eventProcessor = new JiuwenEventProcessor();
        WorkflowIrCacheService cacheService = Mockito.mock(WorkflowIrCacheService.class);
        ReflectionTestUtils.setField(eventProcessor, "workflowIrService", cacheService);
        when(cacheService.getWorkflowIr(any(), any())).thenReturn(new WorkflowIrWrapper());

        ExecuteParams executeParams = ExecuteParams.builder()
            .stream(true)
            .sseEmitter(new SseEmitter())
            .debug(true)
            .build();
        eventProcessor.processCancelStream(executeParams, event, result);
        Assertions.assertTrue(result.getNodeRunInfoList().size() > 0);
    }
}
