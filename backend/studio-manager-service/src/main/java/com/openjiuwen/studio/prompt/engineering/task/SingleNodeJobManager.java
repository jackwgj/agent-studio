/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.Context;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobHandlerRegistry;
import com.openjiuwen.studio.prompt.engineering.task.handler.IJobHandler;
import com.openjiuwen.studio.prompt.engineering.task.repository.JobInstanceRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JobManager的简单实现，直接使用spring的定时线程
 *
 */
public class SingleNodeJobManager {

    private final JobInstanceRepository jobInstanceRepository;

    private final JobHandlerRegistry jobHandlerRegistry;

    private final ClusterTaskExecutor clusterTaskExecutor;

    public SingleNodeJobManager(JobInstanceRepository jobInstanceRepository, JobHandlerRegistry jobHandlerRegistry,
        ClusterTaskExecutor clusterTaskExecutor) {
        this.jobInstanceRepository = jobInstanceRepository;
        this.jobHandlerRegistry = jobHandlerRegistry;
        this.clusterTaskExecutor = clusterTaskExecutor;
    }

    public void dispatch(JobInstance jobInstance, String token) {
        IJobHandler jobHandler = jobHandlerRegistry.getBySpecName(
            jobInstance.getContext().getValue(JobParamKeys.JOB_SPEC_NAME));
        if (jobHandler == null) {
            throw new IllegalStateException(
                "Can't find job handler for job spec name " + jobInstance.getJobSpec().getName());
        }
        // 在这里分配执行的handler_id
        jobInstance.setHandlerId(jobHandlerRegistry.getHandlerId());
        jobInstanceRepository.updateHandlerId(jobInstance, jobHandlerRegistry.getHandlerId());
        // 要在registry里注册当前执行的jobinstance的id，最好能够关联到是哪个线程在执行
        jobHandlerRegistry.registerQueueingJob(jobInstance);

        clusterTaskExecutor.dispatch(jobInstance, jobHandler, token);
    }

    public JobInstance create(String jobSpecName, Context ctx) {
        JobInstance result = jobInstanceRepository.create(jobSpecName, ctx, jobHandlerRegistry.getHandlerId());
        result.setProjectId(RequestContextUtils.getRequestProjectId());
        // 直接发起调度了
        dispatch(result, RequestContextUtils.getRequestAuthToken());
        return result;
    }

    public JobInstance createAndDispatch(JobInstance jobInstance, String token) {
        jobInstanceRepository.create(jobInstance);
        dispatch(jobInstance, token);

        return jobInstance;
    }

    public JobInstance createWithoutPersist(String jobSpecName, Context ctx, String workspaceId) {
        return jobInstanceRepository.createWithoutPersist(jobSpecName, ctx, jobHandlerRegistry.getHandlerId(),
            workspaceId);
    }

    public List<JobInstance> findCanExecuteJob(String handlerId) {
        return jobInstanceRepository.canExecuteJobs(handlerId);
    }

    /**
     * 定时扫描可以被执行的任务。
     * 如果有多节点，需要考虑只扫描属于自己handlerId的任务
     * 后续： 因为现在服务节点是有状态的。所以先根据handlerId获取自己的任务
     */
    public void scheduleCanExecute() {
        findCanExecuteJob(jobHandlerRegistry.getHandlerId()).stream()
            .filter(j -> !jobHandlerRegistry.isRegistered(j))
            .forEach(j -> {
                j.setProjectId(RequestContextUtils.getRequestProjectId());
                dispatch(j, RequestContextUtils.getRequestAuthToken());
            });
    }

    /**
     * 当前服务节点是有状态，文件数据存储在本地，因此需要定时
     * 1. 服务重启，继续执行之前的任务，定时扫描数据库中自己手上的running任务，根据HandlerID匹配，提交执行。
     * 2. 定时扫描属于自己的running的任务，然后检查是否依然在执行，如果不再，则重新提交执行。（一般不会发生这个情况）
     * 后续： 后续如果使用obs这样共享的文件系统，则handler节点就是无状态的，则可以使用抢占式的执行任务
     */
    public void checkRunning() {
        List<JobInstance> executingJobs = jobInstanceRepository.findExecutingJob(jobHandlerRegistry.getHandlerId());

        // 找到不再执行的任务，修改为pending
        List<JobInstance> notRunningJobs = executingJobs.stream()
            .filter(j -> !jobHandlerRegistry.isRegistered(j))
            .collect(Collectors.toList());

        if (notRunningJobs.isEmpty()) {
            return;
        }
        jobInstanceRepository.batchQueueing(notRunningJobs);
    }

}
