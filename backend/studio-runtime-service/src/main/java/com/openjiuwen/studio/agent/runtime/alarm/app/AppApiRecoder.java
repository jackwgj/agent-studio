/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm.app;

import static java.lang.Math.max;

import com.openjiuwen.studio.agent.runtime.alarm.BaseAlarm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述
 *
 */
@Slf4j
public class AppApiRecoder {
    // 统计报错的应用信息（Workflow, Agent）
    private static final Map<String, AppInfo> APP_FAIL_MAP = new ConcurrentHashMap<>();

    // 统计已发送告警的应用
    private static final Map<String, Set<BaseAlarm>> APP_ALARM_MAP = new ConcurrentHashMap<>();

    // 统计调用成功的应用的次数
    private static final Map<String, Integer> APP_SUCCESS_MAP = new ConcurrentHashMap<>();

    private AppApiRecoder() {
    }

    /**
     * 记录失败的 app 调用
     *
     * @param projectId 用户 id
     * @param appId workflowId 或者 agentId
     * @param appType 应用类型（workflow, agent）
     * @param errorMessage 报错信息
     */
    public static void recordFailureApp(String projectId, String appId, String appType, String errorMessage) {
        AppInfo appInfo = APP_FAIL_MAP.computeIfAbsent(appId, k -> new AppInfo(projectId, appId, appType));
        appInfo.getMsgList().add(errorMessage);

        log.info("Record api failure, projectId:{}, appType:{}, appId:{}", projectId, appType, appId);
    }

    /**
     * 记录 app 调用成功的次数
     *
     * @param appId workflowId 或者 agentId
     */
    public static void recordSuccessApp(String appId) {
        int count = APP_SUCCESS_MAP.computeIfAbsent(appId, k -> 0);
        APP_SUCCESS_MAP.put(appId, count + 1);

        log.info("Record api success, appId:{}", appId);
    }

    /**
     * 检查 app 调用情况：
     * 1. 失败次数多的发送告警
     * 2. 没有失败，且成功次数多的，消除告警
     * 3. 清除缓存
     *
     * @param maxFailCount 最多失败次数
     * @param minSuccessCount 最少成功次数
     */
    public static void checkAppCall(int maxFailCount, int minSuccessCount) {
        log.info("Check app call, maxFailCount:{}, minSuccessCount:{}", maxFailCount, minSuccessCount);
        checkAppFailure(maxFailCount);
        clearCache();
    }

    private static void checkAppFailure(int maxFailCount) {
        for (Map.Entry<String, AppInfo> entry : APP_FAIL_MAP.entrySet()) {
            AppInfo info = entry.getValue();
            int failCount = info.msgList.size();
            if (failCount >= maxFailCount) {
                AppFailureAlarm alarm = new AppFailureAlarm();
                alarm.setProjectId(info.getProjectId());
                alarm.getLocationInfo().put("failCount", String.valueOf(failCount));
                alarm.getLocationInfo().put("appId", info.appId);
                alarm.getLocationInfo().put("appType", info.appType);
                List<String> lastThreeMsgs = info.msgList.subList(max(0, failCount - 3), failCount);
                String result = String.join("#", lastThreeMsgs);
                alarm.getLocationInfo().put("errorMessage", result);

                Set<BaseAlarm> set = APP_ALARM_MAP.computeIfAbsent(info.appId, k -> new HashSet<>());
                set.add(alarm);
            }
        }
    }

    private static void clearCache() {
        APP_FAIL_MAP.clear();
        APP_SUCCESS_MAP.clear();
    }

    @Data
    private static class AppInfo {
        private String projectId;

        private String appId;

        private String appType;

        private List<String> msgList;

        AppInfo(String projectId, String appId, String appType) {
            this.projectId = projectId;
            this.appId = appId;
            this.appType = appType;
            this.msgList = new ArrayList<>();
        }
    }
}
