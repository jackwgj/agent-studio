/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskDetailVo;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PromptTaskStatusEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenJobDeatails;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenPromptDeatilRes;
import com.openjiuwen.studio.prompt.engineering.service.v2.job.JiuWenPromptTaskJob;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class PromptOptimizeTaskServiceTest {
    @Spy
    @InjectMocks
    private PromptOptimizeTaskService promptOptimizeTaskService;

    @Mock
    private Scheduler scheduler;

    @Mock
    private PromptTaskMapper promptTaskMapper;

    @Mock
    private JiuWenPromptTaskJob jiuWenPromptTaskJob;

    // 共用测试参数
    private static final String TASK_ID = "task1";

    private static final String PROJECT_ID = "proj1";

    private static final String WORKSPACE_ID = "ws1";

    private static final String TOKEN = "auth_token";

    private static final String JIUWEN_TASK_ID = "jiuwen123";

    /**
     * 测试 submitTask：立即执行（执行时间 ≤ 当前时间）
     */
    @Test
    void test_submitTask_execute_immediately() {
        // 1. 准备任务详情：执行时间 = 当前时间（立即执行）
        Date now = new Date();
        PromptTaskDetailVo taskVo = PromptTaskDetailVo.builder().id(TASK_ID).execTime(now) // 执行时间 ≤ 当前时间
            .projectId(PROJECT_ID).workspaceId(WORKSPACE_ID).build();

        // 2. Mock 静态方法（RequestContextUtils 获取 token）
        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            // 3. 执行 submitTask
            promptOptimizeTaskService.submitTask(taskVo);

            // 4. 验证逻辑：调用 execTask，未调用调度器和状态更新（立即执行无需调度）
            verify(jiuWenPromptTaskJob).execTask(eq(taskVo), eq(TOKEN));
            verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
            verify(promptTaskMapper, never()).updateStatusByPrimaryKey(anyString(), anyInt(), anyString(), anyString());
        } catch (SchedulerException e) {
            throw new AgentStudioException("test error");
        }
    }

    /**
     * 测试 submitTask：定时执行（执行时间 > 当前时间），且注册成功
     */
    @Test
    void test_submitTask_schedule_success() throws SchedulerException {
        // 1. 准备任务详情：执行时间 = 当前时间 + 10分钟（定时执行）
        Date futureTime = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        PromptTaskDetailVo taskVo = PromptTaskDetailVo.builder()
            .id(TASK_ID)
            .execTime(futureTime)
            .projectId(PROJECT_ID)
            .workspaceId(WORKSPACE_ID)
            .build();

        // 2. 准备 JobDetail 和 Trigger（模拟 Quartz 组件）
        JobDetail jobDetail = JobBuilder.newJob(JiuWenPromptTaskJob.class)
            .withIdentity(TASK_ID, "ONE_TIME_GROUP")
            .build();
        SimpleTrigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger_" + TASK_ID, "ONE_TIME_GROUP")
            .startAt(futureTime)
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
            .build();

        Date scheduledTime = new Date();

        // 3. Mock 静态方法和依赖
        try (MockedStatic<JsonUtils> mockedJsonUtils = mockStatic(JsonUtils.class);
            MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {

            // Mock JsonUtils.toJson（序列化任务参数）
            mockedJsonUtils.when(() -> JsonUtils.toJson(eq(taskVo))).thenReturn("task_json");
            // Mock RequestContextUtils 获取 token
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            // Mock 调度器注册任务成功
            when(scheduler.scheduleJob(any(JobDetail.class), any(SimpleTrigger.class))).thenReturn(scheduledTime);

            // 4. 执行 submitTask
            promptOptimizeTaskService.submitTask(taskVo);

            // 5. 验证逻辑
            // 5.1 验证 JobDetail 和 Trigger 的构建（通过参数匹配）
            verify(scheduler).scheduleJob(argThat(
                    job -> job.getKey().getName().equals(TASK_ID) && job.getKey().getGroup().equals("ONE_TIME_GROUP")),
                argThat(trig -> trig.getKey().getName().equals("trigger_" + TASK_ID) && trig.getKey()
                    .getGroup()
                    .equals("ONE_TIME_GROUP")));
            // 5.2 验证状态更新为 WAITING
            verify(promptTaskMapper).updateStatusByPrimaryKey(eq(TASK_ID), eq(PromptTaskStatusEnum.WAITING.getCode()),
                eq(PROJECT_ID), eq(WORKSPACE_ID));
            // 5.3 验证 JsonUtils 和 RequestContextUtils 被调用
            mockedJsonUtils.verify(() -> JsonUtils.toJson(eq(taskVo)));
            mockedRequestContext.verify(RequestContextUtils::getRequestAuthToken);
        }
    }

    /**
     * 测试 submitTask：定时执行失败（SchedulerException），状态更新为 FAILED
     */
    @Test
    void test_submitTask_schedule_failed() throws SchedulerException {
        // 1. 准备任务详情（定时执行）
        Date futureTime = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        PromptTaskDetailVo taskVo = PromptTaskDetailVo.builder()
            .id(TASK_ID)
            .execTime(futureTime)
            .projectId(PROJECT_ID)
            .workspaceId(WORKSPACE_ID)
            .build();

        // 2. Mock 静态方法和依赖（调度器抛出异常）
        try (MockedStatic<JsonUtils> mockedJsonUtils = mockStatic(JsonUtils.class);
            MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {

            mockedJsonUtils.when(() -> JsonUtils.toJson(eq(taskVo))).thenReturn("task_json");
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            // Mock 调度器注册失败，抛出 SchedulerException
            when(scheduler.scheduleJob(any(JobDetail.class), any(SimpleTrigger.class))).thenThrow(
                new SchedulerException("调度失败"));

            // 3. 执行并验证抛出异常
            AgentStudioException exception = assertThrows(AgentStudioException.class, () -> {
                promptOptimizeTaskService.submitTask(taskVo);
            });

            // 验证状态更新为 FAILED
            verify(promptTaskMapper).updateStatusByPrimaryKey(eq(TASK_ID), eq(PromptTaskStatusEnum.FAILED.getCode()),
                eq(PROJECT_ID), eq(WORKSPACE_ID));
            verify(scheduler).scheduleJob(any(JobDetail.class), any(SimpleTrigger.class));
        }
    }

    /**
     * 测试 cancelTask：取消存在的任务，返回 true
     */
    @Test
    void test_cancelTask_success() throws SchedulerException {
        // 1. 构建任务和触发器的 Key
        JobKey jobKey = JobKey.jobKey(TASK_ID, "ONE_TIME_GROUP");
        TriggerKey triggerKey = TriggerKey.triggerKey("trigger_" + TASK_ID, "ONE_TIME_GROUP");

        // 2. Mock 调度器方法调用成功
        doNothing().when(scheduler).pauseTrigger(eq(triggerKey));
        when(scheduler.unscheduleJob(eq(triggerKey))).thenReturn(true);
        when(scheduler.deleteJob(eq(jobKey))).thenReturn(true);

        // 3. 执行 cancelTask
        boolean result = promptOptimizeTaskService.cancelTask(TASK_ID);

        // 4. 验证逻辑
        assertTrue(result);
        verify(scheduler).pauseTrigger(eq(triggerKey));
        verify(scheduler).unscheduleJob(eq(triggerKey));
        verify(scheduler).deleteJob(eq(jobKey));
    }

    /**
     * 测试 cancelTask：取消不存在的任务，返回 false
     */
    @Test
    void test_cancelTask_failed() throws SchedulerException {
        // 1. 构建 Key，Mock 调度器删除失败
        JobKey jobKey = JobKey.jobKey(TASK_ID, "ONE_TIME_GROUP");
        TriggerKey triggerKey = TriggerKey.triggerKey("trigger_" + TASK_ID, "ONE_TIME_GROUP");

        doNothing().when(scheduler).pauseTrigger(eq(triggerKey)); // pauseTrigger 是 void，用 doNothing()
        when(scheduler.unscheduleJob(eq(triggerKey))).thenReturn(true); // unscheduleJob 有返回值，用 thenReturn
        when(scheduler.deleteJob(eq(jobKey))).thenReturn(false); // 删除失败

        // 2. 执行 cancelTask
        boolean result = promptOptimizeTaskService.cancelTask(TASK_ID);

        // 3. 验证逻辑
        assertFalse(result);
        verify(scheduler).deleteJob(eq(jobKey));
    }

    /**
     * 测试 deleteTask：组合 cancelTask 和 deleteTask（九问接口）
     */
    @Test
    void test_deleteTask() {
        // 1. Mock Spy 对象的 cancelTask 方法，使其返回 true（无需执行真实 cancelTask 逻辑）
        doReturn(true).when(promptOptimizeTaskService).cancelTask(eq(TASK_ID));

        // 2. Mock RequestContextUtils 获取 token
        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            PromptTaskDetailVo promptTaskDetailVo = mock(PromptTaskDetailVo.class);
            when(promptTaskDetailVo.getId()).thenReturn(TASK_ID);

            // 3. 执行 deleteTask
            promptOptimizeTaskService.deleteTask(promptTaskDetailVo);

            // 4. 验证逻辑
            // 验证 cancelTask 被调用（Spy 监控自身方法）
            verify(promptOptimizeTaskService).cancelTask(eq(TASK_ID));
        }
    }

    /**
     * 测试 deleteTask：组合 cancelTask 和 deleteTask（九问接口）
     */
    @Test
    void test_deleteTask2() {
        // 1. Mock Spy 对象的 cancelTask 方法，使其返回 true（无需执行真实 cancelTask 逻辑）
        doReturn(true).when(promptOptimizeTaskService).cancelTask(eq(TASK_ID));

        // 2. Mock RequestContextUtils 获取 token
        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            PromptTaskDetailVo promptTaskDetailVo = mock(PromptTaskDetailVo.class);
            when(promptTaskDetailVo.getId()).thenReturn(TASK_ID);
            when(promptTaskDetailVo.getJiuwenTaskId()).thenReturn(JIUWEN_TASK_ID);

            // 3. 执行 deleteTask
            promptOptimizeTaskService.deleteTask(promptTaskDetailVo);

            // 4. 验证逻辑
            // 验证 cancelTask 被调用（Spy 监控自身方法）
            verify(promptOptimizeTaskService).cancelTask(eq(TASK_ID));
            // 验证九问删除接口被调用
            verify(jiuWenPromptTaskJob).deleteTask(eq(promptTaskDetailVo), eq(TOKEN));
        }
    }

    /**
     * 测试 getTaskDetail：调用九问接口查询详情
     */
    @Test
    void test_getTaskDetail() {
        // 1. 准备返回数据
        JiuWenPromptDeatilRes detailRes = new JiuWenPromptDeatilRes();
        PromptTaskDetailVo promptTaskDetailVo = mock(PromptTaskDetailVo.class);
        when(promptTaskDetailVo.getJiuwenTaskId()).thenReturn(JIUWEN_TASK_ID);
        when(promptTaskDetailVo.getPtType()).thenReturn(PtTypeEnum.TEXT);

        when(jiuWenPromptTaskJob.getTaskDetail(eq(promptTaskDetailVo), anyString())).thenReturn(detailRes);

        // 2. Mock RequestContextUtils 获取 token
        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            // 3. 执行 getTaskDetail
            JiuWenPromptDeatilRes result = promptOptimizeTaskService.getTaskDetail(promptTaskDetailVo);

            // 4. 验证逻辑
            assertNotNull(result);
            verify(jiuWenPromptTaskJob).getTaskDetail(eq(promptTaskDetailVo), eq(TOKEN));
        }
    }

    /**
     * 测试 pauseTask 和 resumeTask：调用九问接口暂停/恢复任务
     */
    @Test
    void test_pauseAndResumeTask() {
        // Mock RequestContextUtils 获取 token
        try (MockedStatic<RequestContextUtils> mockedRequestContext = mockStatic(RequestContextUtils.class)) {
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            PromptTaskDetailVo promptTaskDetailVo = mock(PromptTaskDetailVo.class);

            // 测试 pauseTask
            promptOptimizeTaskService.pauseTask(promptTaskDetailVo);
            verify(jiuWenPromptTaskJob).pauseTask(eq(promptTaskDetailVo), eq(TOKEN));

            // 测试 resumeTask
            promptOptimizeTaskService.resumeTask(promptTaskDetailVo);
            verify(jiuWenPromptTaskJob).resumeTask(eq(promptTaskDetailVo), eq(TOKEN));
        }
    }

    @Test
    public void test_queryTaskDetailsByIds_empty_list() throws Exception {
        List<PromptTaskEntity> jiuwenTaskIds = new ArrayList<>();
        try (MockedStatic<RequestContextUtils> contextUtilsMock = mockStatic(RequestContextUtils.class)) {
            contextUtilsMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");
            JiuWenJobDeatails jiuWenJobDeatails = JiuWenJobDeatails.builder()
                .totalJobs(0)
                .finishedJobs(0)
                .runningJobs(0)
                .stoppedJobs(0)
                .failedJobs(0)
                .data(new ArrayList<>())
                .build();
            when(jiuWenPromptTaskJob.queryMultiTaskDetailsByIds(anyList(), anyString())).thenReturn(jiuWenJobDeatails);
            when(jiuWenPromptTaskJob.queryTaskDetailsByIds(anyList(), anyString())).thenReturn(jiuWenJobDeatails);

            JiuWenJobDeatails result = promptOptimizeTaskService.queryTaskDetailsByIds(jiuwenTaskIds);
            assertNotNull(result);
            assertEquals(0, result.getTotalJobs());
        }
    }



    @Test
    void test_queryTaskDetailsByIds_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
                RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn(null);

                when(jiuWenPromptTaskJob.queryTaskDetailsByIds(anyList(), anyString())).thenReturn(null);
                when(jiuWenPromptTaskJob.queryMultiTaskDetailsByIds(anyList(), anyString())).thenReturn(null);

                // When
                JiuWenJobDeatails result = promptOptimizeTaskService.queryTaskDetailsByIds(null);
            }
        });
    }



}
