/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 功能描述
 *
 */
public class QuartzManagerServiceTest extends BaseTest {
    @Autowired
    private QuartzManagerService quartzManagerService;

    @Mock
    private Scheduler scheduler;

    @Test
    void testAddJob() {
        JobDetail jobDetail =
            JobBuilder.newJob(AgentTriggerService.class).withIdentity("test_job_name").build();
        quartzManagerService.addOrUpdateJob(jobDetail, "test_job_name", null, "0/10 * * * * ?");
    }

    @Test
    void testUpdateJob() throws SchedulerException {
        ReflectionTestUtils.setField(quartzManagerService, "scheduler", scheduler);
        when(scheduler.getTrigger(any())).thenReturn(TriggerBuilder.newTrigger()
            .withIdentity("test_job_name")
            .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?"))
            .build());
        JobDetail jobDetail =
            JobBuilder.newJob(AgentTriggerService.class).withIdentity("test_job_name").build();
        quartzManagerService.addOrUpdateJob(jobDetail, "test_job_name", null, "0/10 * * * * ?");
    }

    @Test
    void testDeleteJob() {
        quartzManagerService.deleteJob("test_job_name", null);
    }

}
