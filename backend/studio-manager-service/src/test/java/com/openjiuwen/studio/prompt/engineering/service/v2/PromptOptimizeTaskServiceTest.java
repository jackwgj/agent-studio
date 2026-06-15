/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptBuildRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptFeedbackRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskDetailVo;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PromptTaskStatusEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenJobDeatails;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenPromptDeatilRes;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenProgress;
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

import reactor.core.publisher.Flux;

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
    void test_submitTask_execute_immediately() throws SchedulerException {
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
            assertEquals(StudioError.SCHEDULER_REGISTER_EXCEPTION, exception.getErrorCode());

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
    void test_queryTaskDetailsByIds_NullInput_Throws() {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> promptOptimizeTaskService.queryTaskDetailsByIds(null));
            assertEquals(StudioError.QUERY_OPTIMIZATION_TASK_ERROR, exception.getErrorCode());
        }
    }

    // ========== 补充测试 ==========

    /**
     * 测试 submitTask：execTime 为 null 抛异常
     */
    @Test
    void test_submitTask_execTimeNull_Throws() {
        PromptTaskDetailVo taskVo = PromptTaskDetailVo.builder()
            .id(TASK_ID)
            .execTime(null)
            .projectId(PROJECT_ID)
            .workspaceId(WORKSPACE_ID)
            .build();

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> promptOptimizeTaskService.submitTask(taskVo));
        assertEquals(StudioError.PROMPT_TASK_EXEC_TIME_NOT_SET, exception.getErrorCode());
    }

    /**
     * 测试 cancelTask：SchedulerException 返回 false
     */
    @Test
    void test_cancelTask_SchedulerException_ReturnsFalse() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(TASK_ID, "ONE_TIME_GROUP");
        TriggerKey triggerKey = TriggerKey.triggerKey("trigger_" + TASK_ID, "ONE_TIME_GROUP");

        doThrow(new SchedulerException("调度异常")).when(scheduler).pauseTrigger(eq(triggerKey));

        boolean result = promptOptimizeTaskService.cancelTask(TASK_ID);
        assertFalse(result);
    }

    /**
     * 测试 queryTaskDetailsByIds：混合 TEXT 和 MULTI 类型任务
     */
    @Test
    void test_queryTaskDetailsByIds_MixedTypes() throws Exception {
        PromptTaskEntity textTask = mock(PromptTaskEntity.class);
        when(textTask.getPtType()).thenReturn(PtTypeEnum.TEXT.toString());
        when(textTask.getJiuwenTaskId()).thenReturn("jiuwen-text-001");

        PromptTaskEntity multiTask = mock(PromptTaskEntity.class);
        when(multiTask.getPtType()).thenReturn(PtTypeEnum.MULTI.toString());
        when(multiTask.getJiuwenTaskId()).thenReturn("jiuwen-multi-001");

        List<PromptTaskEntity> tasks = List.of(textTask, multiTask);

        JiuWenProgress textProgress = new JiuWenProgress();
        textProgress.setStatus("running");

        JiuWenJobDeatails textDetails = JiuWenJobDeatails.builder()
            .totalJobs(1).finishedJobs(0).runningJobs(1).stoppedJobs(0).failedJobs(0)
            .data(List.of(textProgress))
            .build();

        JiuWenProgress multiProgress = new JiuWenProgress();
        multiProgress.setStatus("success");

        JiuWenJobDeatails multiDetails = JiuWenJobDeatails.builder()
            .totalJobs(1).finishedJobs(1).runningJobs(0).stoppedJobs(0).failedJobs(0)
            .data(List.of(multiProgress))
            .build();

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            when(jiuWenPromptTaskJob.queryTaskDetailsByIds(List.of("jiuwen-text-001"), TOKEN)).thenReturn(textDetails);
            when(jiuWenPromptTaskJob.queryMultiTaskDetailsByIds(List.of("jiuwen-multi-001"), TOKEN)).thenReturn(multiDetails);

            JiuWenJobDeatails result = promptOptimizeTaskService.queryTaskDetailsByIds(tasks);

            assertNotNull(result);
            assertEquals(2, result.getTotalJobs());
            assertEquals(1, result.getFinishedJobs());
            assertEquals(1, result.getRunningJobs());
            assertEquals(2, result.getData().size());
        }
    }

    /**
     * 测试 queryTaskDetailsByIds：仅 TEXT 类型任务
     */
    @Test
    void test_queryTaskDetailsByIds_TextOnly() throws Exception {
        PromptTaskEntity textTask = mock(PromptTaskEntity.class);
        when(textTask.getPtType()).thenReturn(PtTypeEnum.TEXT.toString());
        when(textTask.getJiuwenTaskId()).thenReturn("jiuwen-text-001");

        List<PromptTaskEntity> tasks = List.of(textTask);

        JiuWenJobDeatails textDetails = JiuWenJobDeatails.builder()
            .totalJobs(1).finishedJobs(1).runningJobs(0).stoppedJobs(0).failedJobs(0)
            .data(new ArrayList<>())
            .build();

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            when(jiuWenPromptTaskJob.queryTaskDetailsByIds(List.of("jiuwen-text-001"), TOKEN)).thenReturn(textDetails);

            JiuWenJobDeatails result = promptOptimizeTaskService.queryTaskDetailsByIds(tasks);

            assertNotNull(result);
            assertEquals(1, result.getTotalJobs());
            assertEquals(1, result.getFinishedJobs());
        }
    }

    /**
     * 测试 queryTaskDetailsByIds：仅 MULTI 类型任务
     */
    @Test
    void test_queryTaskDetailsByIds_MultiOnly() throws Exception {
        PromptTaskEntity multiTask = mock(PromptTaskEntity.class);
        when(multiTask.getPtType()).thenReturn(PtTypeEnum.MULTI.toString());
        when(multiTask.getJiuwenTaskId()).thenReturn("jiuwen-multi-001");

        List<PromptTaskEntity> tasks = List.of(multiTask);

        JiuWenJobDeatails multiDetails = JiuWenJobDeatails.builder()
            .totalJobs(1).finishedJobs(0).runningJobs(0).stoppedJobs(1).failedJobs(0)
            .data(new ArrayList<>())
            .build();

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            when(jiuWenPromptTaskJob.queryMultiTaskDetailsByIds(List.of("jiuwen-multi-001"), TOKEN)).thenReturn(multiDetails);

            JiuWenJobDeatails result = promptOptimizeTaskService.queryTaskDetailsByIds(tasks);

            assertNotNull(result);
            assertEquals(1, result.getTotalJobs());
            assertEquals(1, result.getStoppedJobs());
        }
    }

    /**
     * 测试 queryTaskDetailsByIds：异常抛 QUERY_OPTIMIZATION_TASK_ERROR
     */
    @Test
    void test_queryTaskDetailsByIds_Exception_Throws() throws Exception {
        PromptTaskEntity textTask = mock(PromptTaskEntity.class);
        when(textTask.getPtType()).thenReturn(PtTypeEnum.TEXT.toString());
        when(textTask.getJiuwenTaskId()).thenReturn("jiuwen-text-001");

        List<PromptTaskEntity> tasks = List.of(textTask);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);
            when(jiuWenPromptTaskJob.queryTaskDetailsByIds(anyList(), anyString()))
                .thenThrow(new RuntimeException("查询异常"));

            AgentStudioException exception = assertThrows(AgentStudioException.class,
                () -> promptOptimizeTaskService.queryTaskDetailsByIds(tasks));
            assertEquals(StudioError.QUERY_OPTIMIZATION_TASK_ERROR, exception.getErrorCode());
        }
    }

    /**
     * 测试 generatePrompt：正常生成
     */
    @Test
    void test_generatePrompt_Success() {
        String projectId = PROJECT_ID;
        String workspaceId = WORKSPACE_ID;
        PromptBuildRequest body = new PromptBuildRequest();
        body.setInstruct("帮我写一个提示词");

        Flux<String> fluxResult = Flux.just("生成", "的", "提示词");
        when(jiuWenPromptTaskJob.generatePrompt(eq(projectId), eq(workspaceId), eq(body), eq(TOKEN)))
            .thenReturn(fluxResult);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            Flux<String> result = promptOptimizeTaskService.generatePrompt(projectId, workspaceId, body);
            assertNotNull(result);
            verify(jiuWenPromptTaskJob).generatePrompt(eq(projectId), eq(workspaceId), eq(body), eq(TOKEN));
        }
    }

    /**
     * 测试 optimizeFeedback：正常优化反馈
     */
    @Test
    void test_optimizeFeedback_Success() {
        String projectId = PROJECT_ID;
        String workspaceId = WORKSPACE_ID;
        PromptFeedbackRequest body = new PromptFeedbackRequest();
        body.setPrompt("原始提示词");

        Flux<String> fluxResult = Flux.just("优化", "后", "提示词");
        when(jiuWenPromptTaskJob.optimizeFeedback(eq(projectId), eq(workspaceId), eq(body), eq(TOKEN)))
            .thenReturn(fluxResult);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn(TOKEN);

            Flux<String> result = promptOptimizeTaskService.optimizeFeedback(projectId, workspaceId, body);
            assertNotNull(result);
            verify(jiuWenPromptTaskJob).optimizeFeedback(eq(projectId), eq(workspaceId), eq(body), eq(TOKEN));
        }
    }

}
