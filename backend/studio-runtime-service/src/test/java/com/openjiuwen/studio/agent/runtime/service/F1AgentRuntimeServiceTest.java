/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONArray;
import com.openjiuwen.studio.agent.common.utils.CommonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.runtime.dto.ImportRsp;
import com.openjiuwen.studio.agent.common.dto.run.WorkflowListItem;
import com.openjiuwen.studio.agent.common.dto.run.WorkflowListRsp;
import com.openjiuwen.studio.agent.runtime.entity.NoMatchContentEntity;
import com.openjiuwen.studio.agent.runtime.enums.F1AgentNoMatchContent;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.F1AgentTemplate;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

@MockitoSettings(strictness = Strictness.LENIENT)
class F1AgentRuntimeServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ExecutorService fixedThreadPool;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentManagerClient agentManagerClient;

    @InjectMocks
    private F1AgentRuntimeService f1AgentRuntimeService;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Mock
    private MessageSource messageSource;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JiuwenEventProcessor eventProcessor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RuntimeI18nService runtimeI18nService;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-CN");
        ReflectionTestUtils.setField(f1AgentRuntimeService, "fixedThreadPool", fixedThreadPool);
        ReflectionTestUtils.setField(f1AgentRuntimeService, "agentSseTimeoutMilliSec", 1000L);
        ReflectionTestUtils.setField(f1AgentRuntimeService, "opSvcProjectId", "projectId");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        mockedHeaderStatic.close();
    }

    @Test
    void test_executeQuery_should_return_not_null_when_condition() throws Exception {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate f1AgentTemplate = F1AgentTemplate.builder().matchName("string").type("string").build();
        f1AgentTemplates.add(f1AgentTemplate);

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("string", f1AgentTemplates);

        // Then
        assertEquals("string", result.getType());
        assertEquals("string", result.getMatchName());

        // When
        result = f1AgentRuntimeService.executeQuery("a", f1AgentTemplates);
        // Then
        assertNull(result);

        List<F1AgentTemplate> f1AgentTemplates1 = new ArrayList<>();
        F1AgentTemplate f1AgentTemplate1 = F1AgentTemplate.builder().matchName("a").type("string").build();
        f1AgentTemplates1.add(f1AgentTemplate1);

        // When
        result = f1AgentRuntimeService.executeQuery("string", f1AgentTemplates1);

        // Then
        assertEquals("string", result.getType());
        assertEquals(null, result.getMatchName());
    }

    @Test
    void test_runF1AgentStream_should_return_not_null_when_condition() throws Exception {
        try (MockedStatic<CommonUtils> mockedStaticCommonUtil = mockStatic(CommonUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<JSONArray> mockedStaticJSONArray = mockStatic(JSONArray.class, RETURNS_DEEP_STUBS);
            MockedStatic<F1AgentNoMatchContent> mockedStaticF1AgentNoMatchContent =
                mockStatic(F1AgentNoMatchContent.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticCommonUtil.when(() -> CommonUtils.getResourceContent(eq("template/template.json")))
                .thenReturn("string");

            List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
            F1AgentTemplate f1AgentTemplate = F1AgentTemplate.builder()
                .templateName("string")
                .matchName("string")
                .type("string")
                .importAgentsId("string")
                .importWorkflowsId("string")
                .importToolsId("string")
                .appType("string")
                .appName("string")
                .appDesc("string")
                .appIcon("string")
                .appId("string")
                .build();
            f1AgentTemplates.add(f1AgentTemplate);
            mockedStaticJSONArray.when(() -> JSONArray.parseArray(anyString(), eq(F1AgentTemplate.class)))
                .thenReturn(f1AgentTemplates);

            NoMatchContentEntity noMatchContentEntity = NoMatchContentEntity.builder().build();
            mockedStaticF1AgentNoMatchContent.when(() -> F1AgentNoMatchContent.enumOf(anyString()))
                .thenReturn(noMatchContentEntity);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("string");

            ResponseEntity<ImportRsp> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            when(agentManagerClient.importAgents(anyString(), anyString(), any(Resource.class), anyString(),
                anyString(), anyString())).thenReturn(responseEntity);
            ResponseEntity<ImportRsp> responseEntity1 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            when(agentManagerClient.importWorkflows(anyString(), anyString(), any(Resource.class), anyString(),
                anyString())).thenReturn(responseEntity1);
            ResponseEntity<ImportRsp> responseEntity2 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            when(agentManagerClient.importTools(anyString(), anyString(), any(Resource.class), anyString()))
                .thenReturn(responseEntity2);

            when(fixedThreadPool.submit(any(Runnable.class))).thenReturn(null);

            AgentExecuteParams executeParams =
                AgentExecuteParams.builder().conversationId("string").projectId("string").query("string").build();

            // When
            SseEmitter result = f1AgentRuntimeService.runF1AgentStream(executeParams);

            // Then
            assertNotNull(result);

            List<F1AgentTemplate> f1AgentTemplates1 = new ArrayList<>();
            F1AgentTemplate f1AgentTemplate1 = F1AgentTemplate.builder()
                .templateName("string")
                .matchName("a")
                .type("string")
                .importAgentsId("string")
                .importWorkflowsId("string")
                .importToolsId("string")
                .appType("string")
                .appName("string")
                .appDesc("string")
                .appIcon("string")
                .appId("string")
                .build();
            f1AgentTemplates1.add(f1AgentTemplate1);
            mockedStaticJSONArray.when(() -> JSONArray.parseArray(anyString(), eq(F1AgentTemplate.class)))
                .thenReturn(f1AgentTemplates1);
            f1AgentRuntimeService.runF1AgentStream(executeParams);

            executeParams =
                AgentExecuteParams.builder().conversationId("string").projectId("string").query("a").build();
            f1AgentRuntimeService.runF1AgentStream(executeParams);
        }
    }

    @Test
    void test_sendErrorEvent_should_void_when_condition() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            SseEmitter sseEmitter = new SseEmitter();

            Throwable throwable = new Throwable();

            AgentExecuteParams executeParams =
                AgentExecuteParams.builder().isDone(false).startTime(1738984723304L).build();

            // When
            f1AgentRuntimeService.sendErrorEvent(sseEmitter, throwable, executeParams);
        });
    }

    @Test
    void test_importAgent_should_void_when_condition_tools() throws Exception {
        // Given
        ResponseEntity<ImportRsp> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(agentManagerClient.importAgents(anyString(), anyString(), any(Resource.class), anyString(), anyString(),
            anyString())).thenReturn(responseEntity);
        ResponseEntity<ImportRsp> responseEntity1 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(
            agentManagerClient.importWorkflows(anyString(), anyString(), any(Resource.class), anyString(), anyString()))
            .thenReturn(responseEntity1);
        ResponseEntity<ImportRsp> responseEntity2 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(agentManagerClient.importTools(anyString(), anyString(), any(Resource.class), anyString()))
            .thenReturn(responseEntity2);

        F1AgentTemplate template = F1AgentTemplate.builder()
            .templateName("weather.jsonl")
            .importAgentsId("string")
            .importWorkflowsId("string")
            .importToolsId("string")
            .appType("tools")
            .appName("string")
            .appDesc("string")
            .appIcon("string")
            .appId("string")
            .build();

        SseEmitter sseEmitter = new SseEmitter();

        AgentExecuteParams executeParams =
            AgentExecuteParams.builder().conversationId("string").projectId("0ba805c3edf64df48732c5bf47c40e71").build();

        // When
        f1AgentRuntimeService.importAgent(template, sseEmitter, executeParams, "string");

        SseEmitter sseEmitter1 = new SseEmitter();
        when(agentManagerClient.importTools(anyString(), anyString(), any(Resource.class), anyString()))
            .thenReturn(null);
        f1AgentRuntimeService.importAgent(template, sseEmitter1, executeParams, "string");

        SseEmitter sseEmitter2 = new SseEmitter();
        ResponseEntity<ImportRsp> responseEntity3 = new ResponseEntity<>(HttpStatusCode.valueOf(500));
        when(agentManagerClient.importTools(anyString(), anyString(), any(Resource.class), anyString()))
            .thenReturn(responseEntity3);
        f1AgentRuntimeService.importAgent(template, sseEmitter2, executeParams, "string");
    }

    @Test
    void test_importAgent_should_void_when_condition_agents() throws Exception {
        // Given
        ResponseEntity<ImportRsp> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(agentManagerClient.importAgents(anyString(), anyString(), any(Resource.class), anyString(), anyString(),
            anyString())).thenReturn(responseEntity);
        ResponseEntity<ImportRsp> responseEntity1 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(
            agentManagerClient.importWorkflows(anyString(), anyString(), any(Resource.class), anyString(), anyString()))
            .thenReturn(responseEntity1);
        ResponseEntity<ImportRsp> responseEntity2 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(agentManagerClient.importTools(anyString(), anyString(), any(Resource.class), anyString()))
            .thenReturn(responseEntity2);

        F1AgentTemplate template = F1AgentTemplate.builder()
            .templateName("weather.jsonl")
            .importAgentsId("string")
            .importWorkflowsId("string")
            .importToolsId("string")
            .appType("agents")
            .appName("string")
            .appDesc("string")
            .appIcon("string")
            .appId("string")
            .build();

        SseEmitter sseEmitter = new SseEmitter();

        AgentExecuteParams executeParams =
            AgentExecuteParams.builder().conversationId("string").projectId("0ba805c3edf64df48732c5bf47c40e71").build();

        // When
        f1AgentRuntimeService.importAgent(template, sseEmitter, executeParams, "string");

        SseEmitter sseEmitter1 = new SseEmitter();
        ResponseEntity<ImportRsp> responseEntity3 = new ResponseEntity<>(HttpStatusCode.valueOf(500));
        when(agentManagerClient.importAgents(anyString(), anyString(), any(Resource.class), anyString(), anyString(),
            anyString())).thenReturn(responseEntity3);
        f1AgentRuntimeService.importAgent(template, sseEmitter1, executeParams, "string");
    }

    @Test
    void test_importAgent_should_void_when_condition_workflows() throws Exception {
        // Given
        ResponseEntity<ImportRsp> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(agentManagerClient.importAgents(anyString(), anyString(), any(Resource.class), anyString(), anyString(),
            anyString())).thenReturn(responseEntity);
        ResponseEntity<ImportRsp> responseEntity1 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(
            agentManagerClient.importWorkflows(anyString(), anyString(), any(Resource.class), anyString(), anyString()))
            .thenReturn(responseEntity1);
        ResponseEntity<ImportRsp> responseEntity2 = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(agentManagerClient.importTools(anyString(), anyString(), any(Resource.class), anyString()))
            .thenReturn(responseEntity2);
        WorkflowListItem workflowListItem = new WorkflowListItem();
        workflowListItem.setName("string");
        workflowListItem.setDescription("string");
        workflowListItem.setWorkflowId("workflowId");
        List<WorkflowListItem> workflowList = new ArrayList<>();
        workflowList.add(workflowListItem);
        WorkflowListRsp rsp = new WorkflowListRsp();
        rsp.setWorkflowList(workflowList);
        ResponseEntity<WorkflowListRsp> workflowListRsp = new ResponseEntity<>(rsp, HttpStatusCode.valueOf(200));
        when(agentManagerClient.listWorkflows(any(), any(), any())).thenReturn(workflowListRsp);

        F1AgentTemplate template = F1AgentTemplate.builder()
            .templateName("weather.jsonl")
            .importAgentsId("string")
            .importWorkflowsId("string")
            .importToolsId("string")
            .appType("workflows")
            .appName("string")
            .appDesc("string")
            .appIcon("string")
            .appId("string")
            .build();

        SseEmitter sseEmitter = new SseEmitter();

        AgentExecuteParams executeParams =
            AgentExecuteParams.builder().conversationId("string").projectId("0ba805c3edf64df48732c5bf47c40e71").build();

        // When
        f1AgentRuntimeService.importAgent(template, sseEmitter, executeParams, "string");
    }

    @Test
    void test_executeQuery_with_empty_input_should_return_null() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate f1AgentTemplate = F1AgentTemplate.builder().matchName("string").type("string").build();
        f1AgentTemplates.add(f1AgentTemplate);

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("", f1AgentTemplates);

        // Then
        assertNull(result);
    }

    @Test
    void test_executeQuery_with_long_input_should_return_null() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate f1AgentTemplate = F1AgentTemplate.builder().matchName("string").type("string").build();
        f1AgentTemplates.add(f1AgentTemplate);

        // When
        String longQuery = String.join("", Collections.nCopies(1000, "a"));
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery(longQuery, f1AgentTemplates);

        // Then
        assertNull(result);
    }

    @Test
    void test_executeQuery_with_special_characters_should_return_null() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate f1AgentTemplate = F1AgentTemplate.builder().matchName("string").type("string").build();
        f1AgentTemplates.add(f1AgentTemplate);

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("!@#$%^&*", f1AgentTemplates);

        // Then
        assertNull(result);
    }

    @Test
    void test_executeQuery_should_return_null_when_query_is_empty() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate template = F1AgentTemplate.builder().type("string").matchName("string").build();
        f1AgentTemplates.add(template);

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("", f1AgentTemplates);

        // Then
        assertNull(result);
    }

    @Test
    void test_executeQuery_should_return_null_when_templates_are_empty() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("string", f1AgentTemplates);

        // Then
        assertNull(result);
    }

    @Test
    void test_executeQuery_should_return_first_match_when_multiple_matches() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate template1 = F1AgentTemplate.builder().type("type1").matchName("match1").build();
        F1AgentTemplate template2 = F1AgentTemplate.builder().type("type2").matchName("match2").build();
        f1AgentTemplates.add(template1);
        f1AgentTemplates.add(template2);

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("type1 match1", f1AgentTemplates);

        // Then
        assertEquals("type1", result.getType());
        assertEquals("match1", result.getMatchName());
    }

    @Test
    void test_executeQuery_should_handle_special_characters() {
        // Given
        List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
        F1AgentTemplate template = F1AgentTemplate.builder().type("special!@#").matchName("test").build();
        f1AgentTemplates.add(template);

        // When
        F1AgentTemplate result = f1AgentRuntimeService.executeQuery("special!@#", f1AgentTemplates);

        // Then
        assertEquals("special!@#", result.getType());
        assertNull(result.getMatchName());
    }

    @Test
    void test_runF1AgentStream_should_handle_timeout() throws Exception {
        try (MockedStatic<CommonUtils> mockedStaticCommonUtil = mockStatic(CommonUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<JSONArray> mockedStaticJSONArray = mockStatic(JSONArray.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticCommonUtil.when(() -> CommonUtils.getResourceContent(eq("template/template.json")))
                .thenReturn("string");

            List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
            F1AgentTemplate template = F1AgentTemplate.builder().type("string").matchName("string").build();
            f1AgentTemplates.add(template);
            mockedStaticJSONArray.when(() -> JSONArray.parseArray(anyString(), eq(F1AgentTemplate.class)))
                .thenReturn(f1AgentTemplates);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("string");

            AgentExecuteParams executeParams =
                AgentExecuteParams.builder().conversationId("string").projectId("string").query("string").build();

            // When
            SseEmitter result = f1AgentRuntimeService.runF1AgentStream(executeParams);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_runF1AgentStream_should_handle_exception_during_import() throws Exception {
        try (MockedStatic<CommonUtils> mockedStaticCommonUtil = mockStatic(CommonUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<JSONArray> mockedStaticJSONArray = mockStatic(JSONArray.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticCommonUtil.when(() -> CommonUtils.getResourceContent(eq("template/template.json")))
                .thenReturn("string");

            List<F1AgentTemplate> f1AgentTemplates = new ArrayList<>();
            F1AgentTemplate template = F1AgentTemplate.builder().type("string").matchName("string").build();
            f1AgentTemplates.add(template);
            mockedStaticJSONArray.when(() -> JSONArray.parseArray(anyString(), eq(F1AgentTemplate.class)))
                .thenReturn(f1AgentTemplates);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("string");

            doThrow(new RuntimeException("Test exception")).when(fixedThreadPool).submit(any(Runnable.class));

            AgentExecuteParams executeParams =
                AgentExecuteParams.builder().conversationId("string").projectId("string").query("string").build();

            // When
            SseEmitter result = f1AgentRuntimeService.runF1AgentStream(executeParams);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_updateResourceAndTemplate_should_handle_no_workflow_found() throws Exception {
        // Given
        WorkflowListRsp rsp = new WorkflowListRsp();
        rsp.setWorkflowList(Collections.emptyList());
        ResponseEntity<WorkflowListRsp> workflowListRsp = new ResponseEntity<>(rsp, HttpStatusCode.valueOf(200));

        F1AgentTemplate template = F1AgentTemplate.builder().appName("string").templateName("weather.jsonl").build();

        when(agentManagerClient.listWorkflows(any(), any(), any())).thenReturn(workflowListRsp);

        // When
        Resource result = f1AgentRuntimeService.updateResourceAndTemplate(template, "content",
            new ByteArrayResource(new byte[0]), "token", "projectId");

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getResource_should_handle_empty_content() {
        // Given
        String content = "";
        String name = "empty.json";

        // When
        Resource result = f1AgentRuntimeService.getResource(content, name);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getResource_should_handle_special_characters_in_name() {
        // Given
        String content = "test content";
        String name = "special!@#.json";

        // When
        Resource result = f1AgentRuntimeService.getResource(content, name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getFilename());
    }
}
