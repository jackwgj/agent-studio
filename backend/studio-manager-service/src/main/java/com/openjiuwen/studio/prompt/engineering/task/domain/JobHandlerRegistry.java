/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.domain;

import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.handler.IJobHandler;

import lombok.Data;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述
 *
 */
public class JobHandlerRegistry {

    private final Map<String, IJobHandler> lookupBySpecName;

    @Getter
    private final String handlerId;

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RunningJobInfo>>
        runningJobInfosLookupBySpecName;

    public JobHandlerRegistry(Map<String, IJobHandler> lookupBySpecName, String handlerId) {
        this.lookupBySpecName = lookupBySpecName;
        this.handlerId = handlerId;
        this.runningJobInfosLookupBySpecName = new ConcurrentHashMap<>();
    }

    public IJobHandler getBySpecName(String jobSpecName) {
        return lookupBySpecName.get(jobSpecName);
    }

    public void registerQueueingJob(JobInstance jobInstance) {
        String jobSpecName = getJobSpecName(jobInstance);
        ConcurrentHashMap<String, RunningJobInfo> jobInfos =
            runningJobInfosLookupBySpecName.getOrDefault(jobSpecName, new ConcurrentHashMap<>());

        RunningJobInfo runningJobInfo = new RunningJobInfo();
        runningJobInfo.setJobInstanceId(jobInstance.getId());
        runningJobInfo.setJobSpecName(jobSpecName);
        jobInfos.put(runningJobInfo.getJobInstanceId(), runningJobInfo);

        runningJobInfosLookupBySpecName.put(jobSpecName, jobInfos);

    }

    public void toRunning(JobInstance jobInstance) {
        String jobSpecName = getJobSpecName(jobInstance);

        runningJobInfosLookupBySpecName.get(jobSpecName)
            .get(jobInstance.getId())
            .setThreadName(Thread.currentThread().getName());
    }

    private String getJobSpecName(JobInstance jobInstance) {
        return jobInstance.getContext().getValue(JobParamKeys.JOB_SPEC_NAME);
    }

    public void deregisterJob(JobInstance jobInstance) {
        String jobSpecName = getJobSpecName(jobInstance);
        runningJobInfosLookupBySpecName.get(jobSpecName).remove(jobInstance.getId());
    }

    public boolean isRegistered(JobInstance jobInstance) {
        String jobSpecName = getJobSpecName(jobInstance);
        if (!runningJobInfosLookupBySpecName.containsKey(jobSpecName)) {
            return false;
        }

        return runningJobInfosLookupBySpecName.get(jobSpecName).containsKey(jobInstance.getId());
    }

    @Data
    public static class RunningJobInfo {
        private String jobInstanceId;

        private String jobSpecName;

        /**
         * null表示当前在排队！
         */
        private String threadName;
    }
}
