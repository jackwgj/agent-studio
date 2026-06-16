/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuartzManagerServiceTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private RedisClient redisClient;

    @InjectMocks
    private QuartzManagerService quartzManagerService;

    @Test
    void testAddOrUpdateJob_NewJobScheduled() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(null);
        when(scheduler.isShutdown()).thenReturn(false);

        quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?");

        verify(scheduler).scheduleJob(eq(jobDetail), any(Trigger.class));
        verify(scheduler).start();
        verify(lock).unlock();
    }

    @Test
    void testAddOrUpdateJob_SchedulerShutdown() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(null);
        when(scheduler.isShutdown()).thenReturn(true);

        quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?");

        verify(scheduler).scheduleJob(eq(jobDetail), any(Trigger.class));
        verify(scheduler, never()).start();
        verify(lock).unlock();
    }

    @Test
    void testAddOrUpdateJob_ExistingTriggerSameCron() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);
        CronTrigger existingTrigger = mock(CronTrigger.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(existingTrigger);
        when(existingTrigger.getCronExpression()).thenReturn("0 0 * * * ?");

        quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?");

        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
        verify(lock).unlock();
    }

    @Test
    void testAddOrUpdateJob_ExistingTriggerDifferentCron() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);
        CronTrigger existingTrigger = mock(CronTrigger.class);
        TriggerBuilder<CronTrigger> triggerBuilder = mock(TriggerBuilder.class);
        CronTrigger newTrigger = mock(CronTrigger.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(existingTrigger);
        when(existingTrigger.getCronExpression()).thenReturn("0 0 0 * * ?");
        when(existingTrigger.getTriggerBuilder()).thenReturn(triggerBuilder);
        when(triggerBuilder.withIdentity(any(TriggerKey.class))).thenReturn(triggerBuilder);
        when(triggerBuilder.withSchedule(any(CronScheduleBuilder.class))).thenReturn(triggerBuilder);
        when(triggerBuilder.build()).thenReturn(newTrigger);

        quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?");

        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
        verify(lock).unlock();
    }

    @Test
    void testAddOrUpdateJob_LockNotAcquired() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(false);

        quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?");

        verify(scheduler, never()).getTrigger(any(TriggerKey.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
        verify(lock).unlock();
    }

    @Test
    void testAddOrUpdateJob_GetTriggerThrowsSchedulerException() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenThrow(new SchedulerException("test"));

        assertThrows(AgentStudioException.class, () ->
            quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?"));

        verify(lock).unlock();
    }

    @Test
    void testAddOrUpdateJob_LockReleasedOnException() throws Exception {
        RedisLock lock = mock(RedisLock.class);
        JobDetail jobDetail = mock(JobDetail.class);

        when(redisClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(any(Duration.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(null);
        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class)))
            .thenThrow(new SchedulerException("schedule failed"));

        assertThrows(AgentStudioException.class, () ->
            quartzManagerService.addOrUpdateJob(jobDetail, "testJob", "testGroup", "0 0 * * * ?"));

        verify(lock).unlock();
    }

    @Test
    void testDeleteJob_Success() throws Exception {
        quartzManagerService.deleteJob("testJob", "testGroup");

        verify(scheduler).pauseTrigger(any(TriggerKey.class));
        verify(scheduler).unscheduleJob(any(TriggerKey.class));
        verify(scheduler).deleteJob(any(JobKey.class));
    }

    @Test
    void testDeleteJob_SchedulerException() throws Exception {
        doThrow(new SchedulerException("test")).when(scheduler).pauseTrigger(any(TriggerKey.class));

        assertThrows(AgentStudioException.class, () ->
            quartzManagerService.deleteJob("testJob", "testGroup"));
    }

    @Test
    void testRunJobNow_Success() throws Exception {
        quartzManagerService.runJobNow("testJob", "testGroup");

        verify(scheduler).triggerJob(any(JobKey.class));
    }

    @Test
    void testRunJobNow_SchedulerException() throws Exception {
        doThrow(new SchedulerException("test")).when(scheduler).triggerJob(any(JobKey.class));

        assertThrows(AgentStudioException.class, () ->
            quartzManagerService.runJobNow("testJob", "testGroup"));
    }
}
