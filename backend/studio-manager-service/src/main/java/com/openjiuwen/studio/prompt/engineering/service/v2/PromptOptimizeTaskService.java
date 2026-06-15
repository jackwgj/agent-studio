/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptBuildRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptFeedbackRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskDetailVo;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PromptTaskStatusEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenJobDeatails;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenProgress;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenPromptDeatilRes;
import com.openjiuwen.studio.prompt.engineering.service.v2.job.JiuWenPromptTaskJob;
import com.openjiuwen.studio.prompt.engineering.utils.HttpUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PromptOptimizeTaskService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private PromptTaskMapper promptTaskMapper;

    @Autowired
    private JiuWenPromptTaskJob jiuWenPromptTaskJob;

    public void submitTask(PromptTaskDetailVo promptTaskDetailVo) {
        if (promptTaskDetailVo.getExecTime() == null) {
            log.info("ExecTime hasn't been set");
            throw new AgentStudioException(StudioError.PROMPT_TASK_EXEC_TIME_NOT_SET);
        }

        if (promptTaskDetailVo.getExecTime().getTime() <= new Date().getTime()) {
            log.info("immediately exec prompt task");
            jiuWenPromptTaskJob.execTask(promptTaskDetailVo, RequestContextUtils.getRequestAuthToken());
        } else {
            log.info("waite exec prompt task");
            JobDetail jobDetail = JobBuilder.newJob(JiuWenPromptTaskJob.class)
                .withIdentity(promptTaskDetailVo.getId(), "ONE_TIME_GROUP")  // 任务ID + 任务组（唯一标识任务）
                .usingJobData("taskParam", JsonUtils.toJson(promptTaskDetailVo))  // 传递参数（需序列化）
                .usingJobData("token", RequestContextUtils.getRequestAuthToken()) // 新线程传递token信息
                .storeDurably()  // 即使没有触发器，任务也会被持久化
                .requestRecovery(true)  // 若节点宕机，任务会被其他节点恢复执行
                .build();

            // 3. 定义SimpleTrigger（一次性触发）
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + promptTaskDetailVo.getId(), "ONE_TIME_GROUP")  // 触发器ID + 组
                .startAt(promptTaskDetailVo.getExecTime())  // 触发时间（指定的执行时间）
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0)  // 重复次数：0（仅执行一次）
                    .withMisfireHandlingInstructionFireNow())  // 错过触发时立即执行
                .build();

            // 4. 注册任务到调度器
            Date scheduledTime = null;
            try {
                scheduledTime = scheduler.scheduleJob(jobDetail, trigger);
                log.info("schedule task creat successfully:taskId={},excetime = {}", promptTaskDetailVo.getId(),
                    scheduledTime);
                promptTaskMapper.updateStatusByPrimaryKey(promptTaskDetailVo.getId(),
                    PromptTaskStatusEnum.WAITING.getCode(), promptTaskDetailVo.getProjectId(),
                    promptTaskDetailVo.getWorkspaceId());

            } catch (SchedulerException e) {
                log.error("schedule task creat failed:taskId={},excetime = {}", promptTaskDetailVo.getId(),
                    scheduledTime);
                promptTaskMapper.updateStatusByPrimaryKey(promptTaskDetailVo.getId(),
                    PromptTaskStatusEnum.FAILED.getCode(), promptTaskDetailVo.getProjectId(),
                    promptTaskDetailVo.getWorkspaceId());
                throw new AgentStudioException(StudioError.SCHEDULER_REGISTER_EXCEPTION);
            }
        }
    }

    /**
     * 取消指定任务ID的定时任务
     *
     * @return 是否成功取消
     */
    public boolean cancelTask(String taskId) {
        log.info("start to cancel task, taskId: {}", taskId);

        // 构建任务标识
        JobKey jobKey = JobKey.jobKey(taskId, "ONE_TIME_GROUP");
        // 构建触发器标识
        TriggerKey triggerKey = TriggerKey.triggerKey("trigger_" + taskId, "ONE_TIME_GROUP");

        try {
            log.info("pausing trigger, triggerKey: {}", triggerKey.getName());
            scheduler.pauseTrigger(triggerKey);

            log.info("unscheduling job, triggerKey: {}", triggerKey.getName());
            scheduler.unscheduleJob(triggerKey);

            log.info("deleting job, jobKey: {}", jobKey.getName());
            boolean result = scheduler.deleteJob(jobKey);

            log.info("task cancelled successfully, taskId: {}", taskId);
            return result;
        } catch (SchedulerException e) {
            log.error("cancel task failed, taskId: {}, error: {}", taskId, e.getMessage(), e);
            return false;
        }
    }

    public void deleteTask(PromptTaskDetailVo promptTaskDetailVo) {
        cancelTask(promptTaskDetailVo.getId());
        if (StringUtils.isEmpty(promptTaskDetailVo.getJiuwenTaskId())) {
            return;
        }
        jiuWenPromptTaskJob.deleteTask(promptTaskDetailVo, RequestContextUtils.getRequestAuthToken());
    }

    public JiuWenPromptDeatilRes getTaskDetail(PromptTaskDetailVo promptTaskDetailVo) {
        return jiuWenPromptTaskJob.getTaskDetail(promptTaskDetailVo, RequestContextUtils.getRequestAuthToken());
    }

    public JiuWenJobDeatails queryTaskDetailsByIds(List<PromptTaskEntity> jiuwenTaskIds) {
        try {
            if (jiuwenTaskIds == null) {
                throw new AgentStudioException(StudioError.QUERY_OPTIMIZATION_TASK_ERROR);
            }
            log.info("start to query task details by ids, total: {}", jiuwenTaskIds.size());
            List<String> textTaskIds = new ArrayList<>();
            List<String> multiTaskIds = new ArrayList<>();

            jiuwenTaskIds.forEach(jiuwenTask -> {
                if (jiuwenTask.getPtType().equals(PtTypeEnum.TEXT.toString())) {
                    textTaskIds.add(jiuwenTask.getJiuwenTaskId());
                } else if (jiuwenTask.getPtType().equals(PtTypeEnum.MULTI.toString())) {
                    multiTaskIds.add(jiuwenTask.getJiuwenTaskId());
                }
            });

            log.info("querying text tasks, count: {}", textTaskIds.size());
            JiuWenJobDeatails textPromptTaskJob = new JiuWenJobDeatails(0, 0, 0, 0, 0, new ArrayList<>());
            if (!CollectionUtils.isEmpty(textTaskIds)) {
                textPromptTaskJob = jiuWenPromptTaskJob.queryTaskDetailsByIds(textTaskIds,
                    RequestContextUtils.getRequestAuthToken());
            }

            log.info("querying multi tasks, count: {}", multiTaskIds.size());

            JiuWenJobDeatails multiPromptTaskJob = new JiuWenJobDeatails(0, 0, 0, 0, 0, new ArrayList<>());
            if (!CollectionUtils.isEmpty(multiTaskIds)) {
                multiPromptTaskJob = jiuWenPromptTaskJob.queryMultiTaskDetailsByIds(multiTaskIds,
                    RequestContextUtils.getRequestAuthToken());
            }

            return mergeTaskJobDetail(textPromptTaskJob, multiPromptTaskJob);
        } catch (Exception e) {
            log.error("query task details by ids failed, error: {}", e.getMessage(), e);
            throw new AgentStudioException(StudioError.QUERY_OPTIMIZATION_TASK_ERROR);
        }
    }

    public void pauseTask(PromptTaskDetailVo promptTaskDetailVo) {
        jiuWenPromptTaskJob.pauseTask(promptTaskDetailVo, RequestContextUtils.getRequestAuthToken());
    }

    public void resumeTask(PromptTaskDetailVo promptTaskDetailVo) {
        jiuWenPromptTaskJob.resumeTask(promptTaskDetailVo, RequestContextUtils.getRequestAuthToken());
    }


    public Flux<String> generatePrompt(String projectId, String workspaceId, PromptBuildRequest body) {
        if (Strings.CI.equals(HttpUtil.getLanguage(), CommonConstant.EN_LANGUAGE)) {
            body.setInstruct(body.getInstruct() + "\n 使用英文输出");
        }
        return jiuWenPromptTaskJob.generatePrompt(projectId, workspaceId, body,
            RequestContextUtils.getRequestAuthToken());
    }


    public Flux<String> optimizeFeedback(String projectId, String workspaceId, PromptFeedbackRequest body) {
        return jiuWenPromptTaskJob.optimizeFeedback(projectId, workspaceId, body,
            RequestContextUtils.getRequestAuthToken());
    }

    private JiuWenJobDeatails mergeTaskJobDetail(JiuWenJobDeatails textPromptTaskJob,
        JiuWenJobDeatails multiPromptTaskJob) {
        JiuWenJobDeatails jiuWenJobDeatails = new JiuWenJobDeatails();
        jiuWenJobDeatails.setTotalJobs(textPromptTaskJob.getTotalJobs() + multiPromptTaskJob.getTotalJobs());
        jiuWenJobDeatails.setFinishedJobs(textPromptTaskJob.getFinishedJobs() + multiPromptTaskJob.getFinishedJobs());
        jiuWenJobDeatails.setFailedJobs(textPromptTaskJob.getFailedJobs() + multiPromptTaskJob.getFailedJobs());
        jiuWenJobDeatails.setRunningJobs(textPromptTaskJob.getRunningJobs() + multiPromptTaskJob.getRunningJobs());
        jiuWenJobDeatails.setStoppedJobs(textPromptTaskJob.getStoppedJobs() + multiPromptTaskJob.getStoppedJobs());
        List<JiuWenProgress> data = new ArrayList<>();
        data.addAll(textPromptTaskJob.getData());
        data.addAll(multiPromptTaskJob.getData());
        jiuWenJobDeatails.setData(data);
        return jiuWenJobDeatails;
    }

}
