/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task;

import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobHandlerRegistry;
import com.openjiuwen.studio.prompt.engineering.task.handler.IJobHandler;

import org.springframework.scheduling.annotation.Async;


/**
 * 功能描述
 *
 */
public class ClusterTaskExecutor {
    private final JobHandlerRegistry jobHandlerRegistry;

    public ClusterTaskExecutor(JobHandlerRegistry jobHandlerRegistry) {
        this.jobHandlerRegistry = jobHandlerRegistry;
    }

    private void doDispatch(JobInstance jobInstance, IJobHandler jobHandler, String token) {
        try {
            ThreadRenamer ignored = new ThreadRenamer("job-" + jobInstance.getId());
            jobHandlerRegistry.toRunning(jobInstance);
            jobHandler.execute(jobInstance, token);
            ignored.close();
        } finally {
            jobHandlerRegistry.deregisterJob(jobInstance);
        }
    }

    @Async
    public void dispatch(JobInstance jobInstance, IJobHandler jobHandler, String token) {
        doDispatch(jobInstance, jobHandler, token);
    }
}
