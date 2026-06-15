/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.ModelListVo;
import com.openjiuwen.studio.prompt.engineering.dto.ModelServiceInfo;
import com.openjiuwen.studio.prompt.engineering.dto.PeOptimizationTaskDto;
import com.openjiuwen.studio.prompt.engineering.dto.PeOptimizationTaskVo;
import com.openjiuwen.studio.prompt.engineering.dto.TemplateOptimizeResp;
import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.enums.ResponseCode;
import com.openjiuwen.studio.prompt.engineering.mapper.DatasetMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeOptimizationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.model.ModelServiceCollection;
import com.openjiuwen.studio.prompt.engineering.task.SingleNodeJobManager;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.service.TransactionalService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * OptimizationTaskService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OptimizationTaskServiceTest {

    @InjectMocks
    private OptimizationTaskService optimizationTaskService;

    @Mock
    private TransactionalService transactionalService;

    @Mock
    private SingleNodeJobManager jobManager;

    @Mock
    private OptimizationTaskTransactionService optimizationTaskTransactionService;

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private ModelManagerService modelManagerService;

    @Mock
    private OptimizationTemplateService optimizationTemplateService;

    @Mock
    private PePromptMapper pePromptMapper;

    @Mock
    private PeOptimizationTaskMapper peOptimizationTaskMapper;

    @Mock
    private PeTaskMapper peTaskMapper;

    @Mock
    private ModelServiceCollection modelServiceCollection;

    @Mock
    private Ciphers ciphers;

    // ========== createOptimization ==========

    @Test
    void testCreateOptimization_TaskNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-999");
        body.setName("优化任务");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-999", workspaceId)).thenReturn(false);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
        assertEquals(StudioError.TASK_ID_IS_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_DatasetNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("优化任务");
        body.setTestSetId("test-999");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-999", workspaceId)).thenReturn(false);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
        assertEquals(StudioError.DATASET_FILE_DOES_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_NameExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("重名优化");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "重名优化", workspaceId)).thenReturn(true);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
        assertEquals(StudioError.OPTIMIZATION_TASK_IS_EXIST, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_PromptsEmpty_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("优化任务");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "优化任务", workspaceId)).thenReturn(false);
        when(pePromptMapper.isPromptIdExist("prompt-001", "task-001", workspaceId)).thenReturn(false);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
        assertEquals(StudioError.PROMPT_TEMPLATE_EMPTY, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_ExceedsQuota_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";

        List<String> promptIds = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            promptIds.add("prompt-" + i);
        }

        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("超额优化");
        body.setTestSetId("test-001");
        body.setPromptIds(promptIds);
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "超额优化", workspaceId)).thenReturn(false);

        for (int i = 0; i < 10; i++) {
            when(pePromptMapper.isPromptIdExist("prompt-" + i, "task-001", workspaceId)).thenReturn(true);
            PePrompt prompt = new PePrompt();
            prompt.setId("prompt-" + i);
            when(pePromptMapper.queryPromptsByIds(Collections.singletonList("prompt-" + i), workspaceId))
                .thenReturn(List.of(prompt));
        }

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
        assertEquals(StudioError.CANDIDATE_TEMPLATE_EXCEEDS_QUOTA, exception.getErrorCode());
    }

    @Test
    void testCreateOptimization_ModelNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("优化任务");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "优化任务", workspaceId)).thenReturn(false);
        when(pePromptMapper.isPromptIdExist("prompt-001", "task-001", workspaceId)).thenReturn(true);

        PePrompt prompt = new PePrompt();
        prompt.setId("prompt-001");
        when(pePromptMapper.queryPromptsByIds(Collections.singletonList("prompt-001"), workspaceId))
            .thenReturn(List.of(prompt));

        when(modelManagerService.listModels(eq(projectId), anyString(), eq(workspaceId))).thenReturn(null);

        when(ciphers.encrypt(anyString())).thenReturn("encrypted-token");

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
            assertEquals(StudioError.PROMPT_MODEL_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    void testCreateOptimization_AssistantModelNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setDeploymentId("deploy-001");

        ModelConfig assistantConfig = new ModelConfig();
        assistantConfig.setDeploymentId("deploy-002");

        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("优化任务");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(modelConfig);
        body.setAssistantConfig(assistantConfig);

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "优化任务", workspaceId)).thenReturn(false);
        when(pePromptMapper.isPromptIdExist("prompt-001", "task-001", workspaceId)).thenReturn(true);

        PePrompt prompt = new PePrompt();
        prompt.setId("prompt-001");
        when(pePromptMapper.queryPromptsByIds(Collections.singletonList("prompt-001"), workspaceId))
            .thenReturn(List.of(prompt));

        ModelServiceInfo modelServiceInfo = new ModelServiceInfo();
        modelServiceInfo.setDeploymentId("deploy-001");
        ModelListVo modelListVo = new ModelListVo();
        modelListVo.setModelServiceInfoList(List.of(modelServiceInfo));
        when(modelManagerService.listModels(eq(projectId), anyString(), eq(workspaceId))).thenReturn(modelListVo);

        when(ciphers.encrypt(anyString())).thenReturn("encrypted-token");

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> optimizationTaskService.createOptimization(projectId, workspaceId, body));
            assertEquals(StudioError.PROMPT_MODEL_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    void testCreateOptimization_NormalCreate_NoRunningTask() {
        String projectId = "project1";
        String workspaceId = "workspace1";

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setDeploymentId("deploy-001");

        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("优化任务");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(modelConfig);
        body.setNumIter(3);
        body.setType("normal");

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "优化任务", workspaceId)).thenReturn(false);
        when(pePromptMapper.isPromptIdExist("prompt-001", "task-001", workspaceId)).thenReturn(true);

        PePrompt prompt = new PePrompt();
        prompt.setId("prompt-001");
        when(pePromptMapper.queryPromptsByIds(Collections.singletonList("prompt-001"), workspaceId))
            .thenReturn(List.of(prompt));

        ModelServiceInfo modelServiceInfo = new ModelServiceInfo();
        modelServiceInfo.setDeploymentId("deploy-001");
        ModelListVo modelListVo = new ModelListVo();
        modelListVo.setModelServiceInfoList(List.of(modelServiceInfo));
        when(modelManagerService.listModels(eq(projectId), anyString(), eq(workspaceId))).thenReturn(modelListVo);

        when(ciphers.encrypt(anyString())).thenReturn("encrypted-token");

        when(peOptimizationTaskMapper.getOptimizationTask(eq(projectId), eq("task-001"),
            eq(OptimizationTaskStatus.executing()), eq(workspaceId))).thenReturn(null);

        JobInstance jobInstance = new JobInstance();
        when(jobManager.createWithoutPersist(anyString(), any(), anyString())).thenReturn(jobInstance);

        PeOptimizationTask savedTask = new PeOptimizationTask();
        savedTask.setId("opt-001");
        savedTask.setName("优化任务");
        savedTask.setStatus(OptimizationTaskStatus.PENDING);
        savedTask.setPrompts(List.of(prompt));
        savedTask.setModelConfig(modelConfig);
        savedTask.setCreatedOn(new Date());
        savedTask.setUpdatedOn(new Date());
        savedTask.setRunningOn(new Date());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(anyString(), eq(workspaceId)))
            .thenReturn(List.of(savedTask));

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            PeOptimizationTaskVo result = optimizationTaskService.createOptimization(projectId, workspaceId, body);
            assertNotNull(result);
            verify(optimizationTaskTransactionService).createOptimizationTask(any(PeOptimizationTask.class));
        }
    }

    @Test
    void testCreateOptimization_NormalCreate_HasRunningTask() {
        String projectId = "project1";
        String workspaceId = "workspace1";

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setDeploymentId("deploy-001");

        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("优化任务");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(modelConfig);
        body.setNumIter(3);
        body.setType("normal");

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "优化任务", workspaceId)).thenReturn(false);
        when(pePromptMapper.isPromptIdExist("prompt-001", "task-001", workspaceId)).thenReturn(true);

        PePrompt prompt = new PePrompt();
        prompt.setId("prompt-001");
        when(pePromptMapper.queryPromptsByIds(Collections.singletonList("prompt-001"), workspaceId))
            .thenReturn(List.of(prompt));

        ModelServiceInfo modelServiceInfo = new ModelServiceInfo();
        modelServiceInfo.setDeploymentId("deploy-001");
        ModelListVo modelListVo = new ModelListVo();
        modelListVo.setModelServiceInfoList(List.of(modelServiceInfo));
        when(modelManagerService.listModels(eq(projectId), anyString(), eq(workspaceId))).thenReturn(modelListVo);

        when(ciphers.encrypt(anyString())).thenReturn("encrypted-token");

        PeOptimizationTask runningTask = new PeOptimizationTask();
        when(peOptimizationTaskMapper.getOptimizationTask(eq(projectId), eq("task-001"),
            eq(OptimizationTaskStatus.executing()), eq(workspaceId))).thenReturn(runningTask);

        PeOptimizationTask savedTask = new PeOptimizationTask();
        savedTask.setId("opt-001");
        savedTask.setName("优化任务");
        savedTask.setStatus(OptimizationTaskStatus.PENDING);
        savedTask.setPrompts(List.of(prompt));
        savedTask.setModelConfig(modelConfig);
        savedTask.setCreatedOn(new Date());
        savedTask.setUpdatedOn(new Date());
        savedTask.setRunningOn(new Date());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(anyString(), eq(workspaceId)))
            .thenReturn(List.of(savedTask));

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");
            mocked.when(RequestContextUtils::getRequestProjectId).thenReturn(projectId);

            PeOptimizationTaskVo result = optimizationTaskService.createOptimization(projectId, workspaceId, body);
            assertNotNull(result);
            verify(optimizationTaskTransactionService).createOptimizationTask(any(PeOptimizationTask.class));
            verify(jobManager, never()).createWithoutPersist(anyString(), any(), anyString());
        }
    }

    @Test
    void testCreateOptimization_RefineType_Success() {
        String projectId = "project1";
        String workspaceId = "workspace1";

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setDeploymentId("deploy-001");

        PeOptimizationTaskDto body = new PeOptimizationTaskDto();
        body.setTaskId("task-001");
        body.setName("精炼优化");
        body.setType("refine");
        body.setTestSetId("test-001");
        body.setPromptIds(new java.util.ArrayList<>(List.of("prompt-001")));
        body.setModelConfig(modelConfig);
        body.setNumIter(3);

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(datasetMapper.isIdExist("test-001", workspaceId)).thenReturn(true);
        when(peOptimizationTaskMapper.isNameExist("task-001", "精炼优化", workspaceId)).thenReturn(false);
        when(pePromptMapper.isPromptIdExist("prompt-001", "task-001", workspaceId)).thenReturn(true);

        PePrompt prompt = new PePrompt();
        prompt.setId("prompt-001");
        prompt.setQuestion("原始提示词");
        when(pePromptMapper.queryPromptsByIds(Collections.singletonList("prompt-001"), workspaceId))
            .thenReturn(List.of(prompt));

        ModelServiceInfo modelServiceInfo = new ModelServiceInfo();
        modelServiceInfo.setDeploymentId("deploy-001");
        ModelListVo modelListVo = new ModelListVo();
        modelListVo.setModelServiceInfoList(List.of(modelServiceInfo));
        when(modelManagerService.listModels(eq(projectId), anyString(), eq(workspaceId))).thenReturn(modelListVo);

        when(ciphers.encrypt(anyString())).thenReturn("encrypted-token");

        InvokeResp invokeResp = new InvokeResp();
        invokeResp.setResult("精炼后的提示词");
        when(modelServiceCollection.callModel(eq(workspaceId), any(), anyString(), eq(projectId)))
            .thenReturn(invokeResp);

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");
            mocked.when(RequestContextUtils::getRequestProjectId).thenReturn(projectId);

            PeOptimizationTaskVo result = optimizationTaskService.createOptimization(projectId, workspaceId, body);
            assertNotNull(result);
            verify(peOptimizationTaskMapper).updateOptimizationTaskResultById(any());
        }
    }

    // ========== changeOptimization ==========

    @Test
    void testChangeOptimization_UpdateName() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setName("旧名称");
        optTask.setStatus(OptimizationTaskStatus.SUCCESS);
        optTask.setCreatedOn(new Date());
        optTask.setUpdatedOn(new Date());
        optTask.setRunningOn(new Date());
        optTask.setPrompts(Collections.emptyList());
        optTask.setModelConfig(new ModelConfig());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        PeOptimizationTaskVo result = optimizationTaskService.changeOptimization(projectId, id, "新名称", "新描述", null, workspaceId);
        assertNotNull(result);
        verify(peOptimizationTaskMapper).modifyOptimizationTaskInfo(eq(id), eq("新名称"), eq("新描述"),
            eq(OptimizationTaskStatus.SUCCESS), eq(workspaceId));
    }

    @Test
    void testChangeOptimization_Restart() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setProjectId(projectId);
        optTask.setTaskId("task-001");
        optTask.setName("优化任务");
        optTask.setJiuwenTaskId("jiuwen-001");
        optTask.setStatus(OptimizationTaskStatus.PAUSED);
        optTask.setCreatedOn(new Date());
        optTask.setUpdatedOn(new Date());
        optTask.setRunningOn(new Date());
        optTask.setPrompts(Collections.emptyList());
        optTask.setModelConfig(new ModelConfig());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));
        when(peOptimizationTaskMapper.getOptimizationTask(projectId, "task-001",
            OptimizationTaskStatus.executing(), workspaceId)).thenReturn(null);

        TemplateOptimizeResp resp = TemplateOptimizeResp.builder().code(ResponseCode.OK.getCode()).build();
        when(optimizationTemplateService.changeOptimization("jiuwen-001", true)).thenReturn(resp);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            var result = optimizationTaskService.changeOptimization(projectId, id, null, null, "restart", workspaceId);
            assertNotNull(result);
            verify(peOptimizationTaskMapper).modifyOptimizationTaskInfo(eq(id), anyString(), any(),
                eq(OptimizationTaskStatus.RUNNING), eq(workspaceId));
        }
    }

    @Test
    void testChangeOptimization_Restart_RunningExist_Throws() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setProjectId(projectId);
        optTask.setTaskId("task-001");
        optTask.setStatus(OptimizationTaskStatus.PAUSED);
        optTask.setCreatedOn(new Date());
        optTask.setUpdatedOn(new Date());
        optTask.setRunningOn(new Date());
        optTask.setPrompts(Collections.emptyList());
        optTask.setModelConfig(new ModelConfig());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        PeOptimizationTask runningTask = new PeOptimizationTask();
        runningTask.setTaskId("task-001");
        when(peOptimizationTaskMapper.getOptimizationTask(projectId, "task-001",
            OptimizationTaskStatus.executing(), workspaceId)).thenReturn(runningTask);
        when(peTaskMapper.queryTaskNameById("task-001", workspaceId)).thenReturn("任务1");

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.changeOptimization(projectId, id, null, null, "restart", workspaceId));
        assertEquals(StudioError.OPTIMIZATION_TASK_RUNNING_IS_EXIST, exception.getErrorCode());
    }

    @Test
    void testChangeOptimization_Stop() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setProjectId(projectId);
        optTask.setTaskId("task-001");
        optTask.setName("优化任务");
        optTask.setJiuwenTaskId("jiuwen-001");
        optTask.setStatus(OptimizationTaskStatus.RUNNING);
        optTask.setCreatedOn(new Date());
        optTask.setUpdatedOn(new Date());
        optTask.setRunningOn(new Date());
        optTask.setPrompts(Collections.emptyList());
        optTask.setModelConfig(new ModelConfig());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        TemplateOptimizeResp resp = TemplateOptimizeResp.builder().code(ResponseCode.OK.getCode()).build();
        when(optimizationTemplateService.changeOptimization("jiuwen-001", false)).thenReturn(resp);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            var result = optimizationTaskService.changeOptimization(projectId, id, null, null, "stop", workspaceId);
            assertNotNull(result);
            verify(peOptimizationTaskMapper).modifyOptimizationTaskInfo(eq(id), anyString(), any(),
                eq(OptimizationTaskStatus.PAUSED), eq(workspaceId));
        }
    }

    @Test
    void testChangeOptimization_InvalidStatus_Throws() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setStatus(OptimizationTaskStatus.SUCCESS);
        optTask.setCreatedOn(new Date());
        optTask.setUpdatedOn(new Date());
        optTask.setRunningOn(new Date());
        optTask.setPrompts(Collections.emptyList());
        optTask.setModelConfig(new ModelConfig());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.changeOptimization(projectId, id, null, null, "invalid", workspaceId));
        assertEquals(StudioError.OPTIMIZATION_TASK_STATUS_IS_INVALIDATE, exception.getErrorCode());
    }

    @Test
    void testChangeOptimization_NotExist_Throws() {
        String projectId = "project1";
        String id = "opt-999";
        String workspaceId = "workspace1";

        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(Collections.emptyList());

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.changeOptimization(projectId, id, "名称", null, null, workspaceId));
        assertEquals(StudioError.OPTIMIZATION_TASK_ID_IS_NOT_EXIST, exception.getErrorCode());
    }

    // ========== showOptimization ==========

    @Test
    void testShowOptimization_Success() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        when(peOptimizationTaskMapper.isOptimizationTaskExist(id, workspaceId)).thenReturn(true);

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setName("优化任务1");
        optTask.setTaskId("task-001");
        optTask.setStatus(OptimizationTaskStatus.SUCCESS);
        optTask.setModelConfig(new ModelConfig());
        optTask.setPrompts(Collections.emptyList());
        optTask.setCreatedOn(new Date());
        optTask.setUpdatedOn(new Date());
        optTask.setRunningOn(new Date());
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        PeOptimizationTaskVo result = optimizationTaskService.showOptimization(projectId, id, workspaceId);
        assertNotNull(result);
    }

    @Test
    void testShowOptimization_NotExist_Throws() {
        String projectId = "project1";
        String id = "opt-999";
        String workspaceId = "workspace1";

        when(peOptimizationTaskMapper.isOptimizationTaskExist(id, workspaceId)).thenReturn(false);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.showOptimization(projectId, id, workspaceId));
        assertEquals(StudioError.OPTIMIZATION_TASK_ID_IS_NOT_EXIST, exception.getErrorCode());
    }

    // ========== deleteOptimization ==========

    @Test
    void testDeleteOptimization_Success() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setName("优化任务");
        optTask.setStatus(OptimizationTaskStatus.SUCCESS);
        optTask.setJiuwenTaskId("jiuwen-001");
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        TemplateOptimizeResp resp = TemplateOptimizeResp.builder().code(ResponseCode.OK.getCode()).build();
        when(optimizationTemplateService.deleteOptimization("jiuwen-001")).thenReturn(resp);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            String result = optimizationTaskService.deleteOptimization(projectId, id, workspaceId);
            assertEquals(HttpStatus.OK.toString(), result);
            verify(optimizationTemplateService).deleteOptimization("jiuwen-001");
            verify(optimizationTaskTransactionService).deleteOptimizationTaskAndResult(id, workspaceId);
        }
    }

    @Test
    void testDeleteOptimization_Running_Throws() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setName("运行中优化");
        optTask.setStatus(OptimizationTaskStatus.RUNNING);
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.deleteOptimization(projectId, id, workspaceId));
        assertEquals(StudioError.OPTIMIZATION_TASK_RUNNING, exception.getErrorCode());
    }

    @Test
    void testDeleteOptimization_PendingNoJiuwenId() {
        String projectId = "project1";
        String id = "opt-001";
        String workspaceId = "workspace1";

        PeOptimizationTask optTask = new PeOptimizationTask();
        optTask.setId(id);
        optTask.setName("等待中优化");
        optTask.setStatus(OptimizationTaskStatus.PENDING);
        optTask.setJiuwenTaskId(null);
        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(List.of(optTask));

        String result = optimizationTaskService.deleteOptimization(projectId, id, workspaceId);
        assertEquals(HttpStatus.OK.toString(), result);
        verify(optimizationTemplateService, never()).deleteOptimization(anyString());
        verify(optimizationTaskTransactionService).deleteOptimizationTaskAndResult(id, workspaceId);
    }

    @Test
    void testDeleteOptimization_NotExist_Throws() {
        String projectId = "project1";
        String id = "opt-999";
        String workspaceId = "workspace1";

        when(peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId))
            .thenReturn(Collections.emptyList());

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> optimizationTaskService.deleteOptimization(projectId, id, workspaceId));
        assertEquals(StudioError.OPTIMIZATION_TASK_ID_IS_NOT_EXIST, exception.getErrorCode());
    }
}
