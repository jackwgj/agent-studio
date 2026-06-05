/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分析事件缓存队列
 *
 */
public class AnalyticsEventQueue {
    private static final List<AnalyticsEventEntity> EVENTS = new ArrayList<>();

    // 最大缓存100000条事件， 超过时，默认丢弃事件最早的事件
    private static final int SIZE = 100000;

    private AnalyticsEventQueue() {
    }

    public static boolean isEmpty() {
        return EVENTS.isEmpty();
    }

    /**
     * 添加事件
     *
     * @param event 事件
     */
    public static void push(AnalyticsEventEntity event) {
        EVENTS.add(event);
        if (EVENTS.size() > SIZE) {
            EVENTS.remove(0);
        }
    }

    /**
     * 取出事件，基于截止时间
     *
     * @param endTime 截止时间
     */
    public static List<AnalyticsEventEntity> popBatchByTime(Date endTime) {
        List<AnalyticsEventEntity> events = new ArrayList<>();
        while (!EVENTS.isEmpty()) {
            if (EVENTS.get(0).getEventTime().before(endTime)) {
                events.add(EVENTS.remove(0));
            } else {
                break;
            }
        }
        return events;
    }
}
