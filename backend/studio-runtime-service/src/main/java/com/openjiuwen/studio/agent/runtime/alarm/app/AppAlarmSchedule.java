/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm.app;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 */
@Component
@Slf4j
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "omservice_endpoint")
public class AppAlarmSchedule {
    @Value("${alarm_app_fail_count_limit:10}")
    private int appFailureCountLimit;

    @Value("${alarm_app_success_count_min:5}")
    private int appSuccessCountLimit;

    /**
     * 5 分钟检查一次接口失败情况
     */
    @Scheduled(cron = "${app_failure_alarm_check_schedule: 0 */5 * * * *}")
    @Async
    public void checkApiFailureCount() {
        AppApiRecoder.checkAppCall(appFailureCountLimit, appSuccessCountLimit);
    }
}
