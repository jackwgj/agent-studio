/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.repository;

import com.openjiuwen.studio.prompt.engineering.mapper.JobInstanceMapper;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobSpec;
import com.openjiuwen.studio.prompt.engineering.task.domain.Context;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobStatus;
import com.openjiuwen.studio.prompt.engineering.task.vo.TaskQueryCondition;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * 功能描述
 *
 */
@Component
public class JobInstanceRepository {
    @Resource
    private JobInstanceMapper jobInstanceMapper;

    public void start(JobInstance jobInstance) {
        LocalDateTime now = LocalDateTime.now();
        jobInstanceMapper.startAt(jobInstance.getId(), now, jobInstance.getWorkspaceId());
        jobInstance.setStatus(JobStatus.running);
        jobInstance.setStartedOn(now);
    }

    /**
     * 创建一个任务 test
     *
     * @param jobSpecName jobSpec任务名称
     * @param ctx 上下文
     * @param handlerId handlerId 后续需要删除，不应该在创建任务的时候就分配handlerId
     * @return 新创建的JobInstance
     */
    public JobInstance create(String jobSpecName, Context ctx, String handlerId) {
        JobInstance jobInstance = createWithoutPersist(jobSpecName, ctx, handlerId, "");
        create(jobInstance);

        return jobInstance;
    }

    /**
     * 创建一个任务JobInstance，但是不持久化
     *
     * @param jobSpecName jobSpec任务名称
     * @param ctx 上下文
     * @param handlerId handlerId 后续需要删除，不应该在创建任务的时候就分配handlerId
     * @return 新创建的JobInstance
     */
    public JobInstance createWithoutPersist(String jobSpecName, Context ctx, String handlerId, String workspaceId) {
        ctx.put(JobParamKeys.JOB_SPEC_NAME, jobSpecName);
        JobSpec jobSpec = jobInstanceMapper.queryJobSpecByName(jobSpecName);

        JobInstance jobInstance = new JobInstance();

        jobInstance.setId(UUID.randomUUID().toString());
        jobInstance.setName(ctx.getValue(JobParamKeys.JOB_NAME));
        LocalDateTime now = LocalDateTime.now();
        jobInstance.setCreatedOn(now);
        jobInstance.setUpdatedOn(now);
        jobInstance.setJobSpecId(jobSpec.getId());
        jobInstance.setRetryTime(0);
        jobInstance.setStatus(JobStatus.pending);
        jobInstance.setHandlerId(handlerId);

        // PE UUID
        jobInstance.setAccountId("bb15c1b2-fc0a-4786-a735-6c34d30a87b2");

        jobInstance.setContext(ctx);

        jobInstance.setJobSpec(jobSpec);
        jobInstance.setWorkspaceId(workspaceId);

        return jobInstance;
    }

    public JobInstance create(JobInstance jobInstance) {
        jobInstanceMapper.create(jobInstance);

        return jobInstance;
    }

    public List<JobInstance> canExecuteJobs(String handlerId) {
        Set<JobStatus> jobStatuses = JobStatus.canExecute();
        TaskQueryCondition taskQueryCondition = TaskQueryCondition.builder().statuses(jobStatuses).handlerId(handlerId)
            .build();
        return jobInstanceMapper.queryWithCondition2(taskQueryCondition);
    }

    public void failed(JobInstance jobInstance) {
        LocalDateTime now = LocalDateTime.now();
        jobInstance.setUpdatedOn(now);
        jobInstance.setEndedOn(now);
        jobInstance.setStatus(JobStatus.failed);

        jobInstanceMapper.update(jobInstance);
    }

    public void retry(JobInstance jobInstance) {
        jobInstance.setRetryTime(jobInstance.getRetryTime() + 1);
        LocalDateTime now = LocalDateTime.now();
        jobInstance.setUpdatedOn(now);
        jobInstance.setStatus(JobStatus.retrying);

        jobInstanceMapper.update(jobInstance);
    }

    public void success(JobInstance jobInstance) {
        LocalDateTime now = LocalDateTime.now();
        jobInstance.setUpdatedOn(now);
        jobInstance.setEndedOn(now);
        jobInstance.setStatus(JobStatus.success);

        jobInstanceMapper.update(jobInstance);
    }

    public void updateHandlerId(JobInstance jobInstance, String handlerId) {
        jobInstance.setHandlerId(handlerId);
        jobInstance.setUpdatedOn(LocalDateTime.now());
        jobInstanceMapper.update(jobInstance);
    }

    public List<JobInstance> findExecutingJob(String handlerId) {
        return jobInstanceMapper.queryWithCondition2(
            TaskQueryCondition.builder().statuses(JobStatus.executing()).handlerId(handlerId).build());
    }

    public void batchQueueing(List<JobInstance> jobs) {
        jobInstanceMapper.batchUpdateStatusByIds(JobStatus.pending,
            jobs.stream().map(j -> j.getId()).collect(Collectors.toList()));
    }

    public void updateContext(JobInstance jobInstance) {
        jobInstanceMapper.updateCtx(jobInstance);
    }

    public void partlySuccess(JobInstance jobInstance, String errorMsg) {
        jobInstance.getContext().put(JobParamKeys.ERROR_MSG, errorMsg);
        LocalDateTime now = LocalDateTime.now();
        jobInstance.setUpdatedOn(now);
        jobInstance.setEndedOn(now);
        jobInstance.setStatus(JobStatus.partlysuccess);

        jobInstanceMapper.update(jobInstance);
    }

    public void updateStatus(JobInstance jobInstance, JobStatus status) {
        jobInstance.setEndedOn(LocalDateTime.now());
        jobInstance.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        jobInstance.setUpdatedOn(now);
        jobInstanceMapper.update(jobInstance);
    }
}
