/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.run.CreateTaskReq;
import com.openjiuwen.studio.agent.common.dto.run.ListTaskQo;
import com.openjiuwen.studio.agent.common.dto.run.TaskListRsp;
import com.openjiuwen.studio.agent.common.dto.run.TaskRsp;
import com.openjiuwen.studio.agent.common.dto.run.TaskStatus;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.common.dto.run.RetrieveTaskQo;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务管理接口
 *
 */
@Slf4j
public class TaskManagementServiceTest extends BaseTest {
    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    private TaskRuntimeService taskRuntimeService;

    @Autowired
    private TaskManagementService taskManagementService;

    @BeforeAll
    public static void before() {
        RequestContextUtils.setRequestAuthTokenAndUserId(ConstantTest.TEST_TOKEN, ConstantTest.TEST_PROJECT_ID,
                ConstantTest.TEST_USER_ID);
    }

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(taskRuntimeService, "workflowRuntimeService", workflowRuntimeService);
        ReflectionTestUtils.setField(taskRuntimeService, "redisClient", redisClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @SneakyThrows
    @Test
    void testManageAsyncTask() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", "你好");
        CreateTaskReq createTaskReq =
                new CreateTaskReq().setType("chat").setName("异步任务").setMode("ASYNC").setInputs(inputs).setTimeout(360000);
        TaskRsp resp = taskManagementService.createTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, null, null, createTaskReq);
        String taskId = resp.getId();
        Assertions.assertEquals(resp.getStatus().getValue(), TaskStatus.INIT.getValue());
        taskManagementService.cancelTask(ConstantTest.TEST_PROJECT_ID, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID,
                taskId, null);
        resp = taskManagementService.retrieveTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, taskId, new RetrieveTaskQo().setWorkspaceId(null));
        Assertions.assertEquals(resp.getStatus().getValue(), TaskStatus.CANCELLED.getValue());
        TaskListRsp listRsp = taskManagementService.listTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, new ListTaskQo().setStatus(TaskStatus.CANCELLED.getValue()));
        Assertions.assertEquals(1L, listRsp.getCount());
        taskManagementService.deleteTask(ConstantTest.TEST_PROJECT_ID, ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID,
                taskId, null);
        listRsp = taskManagementService.listTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, new ListTaskQo().setStatus(TaskStatus.CANCELLED.getValue()));
        Assertions.assertEquals(0L, listRsp.getCount());
    }

    @SneakyThrows
    @Test
    void testRunAsyncTask() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", "你好");
        CreateTaskReq createTaskReq =
                new CreateTaskReq().setType("chat").setName("异步任务").setMode("ASYNC").setInputs(inputs).setTimeout(360000);
        TaskRsp resp = taskManagementService.createTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, null, null, createTaskReq);

        taskRuntimeService.findAndAsyncExecuteWorkflow();
        Thread.sleep(5 * 1000);
        resp = taskManagementService.retrieveTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, resp.getId(), new RetrieveTaskQo().setWorkspaceId(null));
        Assertions.assertEquals(resp.getStatus().getValue(), TaskStatus.FAILED.getValue());
    }

    @SneakyThrows
    @Test
    void testRunAsyncTaskFailed() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", "你好");
        CreateTaskReq createTaskReq = new CreateTaskReq().setType("chat")
                .setName("异步任务")
                .setMode("ASYNC")
                .setInputs(inputs)
                .setTimeout(360000);
        TaskRsp resp = taskManagementService.createTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, null, null, createTaskReq);
        when(workflowRuntimeService.runWorkflow(any()))
                .thenThrow(new AgentStudioException(StudioError.WORKFLOW_ASYNC_GET_LOCK_FAILED));
        taskRuntimeService.findAndAsyncExecuteWorkflow();
        Thread.sleep(5 * 1000);
        resp = taskManagementService.retrieveTask(ConstantTest.TEST_PROJECT_ID,
                ConstantTest.Workflow.TEST_LLM_WORKFLOW_ID, resp.getId(), new RetrieveTaskQo().setWorkspaceId(null));
        Assertions.assertEquals(resp.getStatus().getValue(), TaskStatus.FAILED.getValue());
    }
}
