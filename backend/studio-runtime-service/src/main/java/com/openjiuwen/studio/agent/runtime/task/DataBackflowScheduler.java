/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.task;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.entity.FeedbackEntity;
import com.openjiuwen.studio.agent.runtime.service.ConversationManagementService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 功能描述
 *
 */
@Component
@Slf4j
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "feedback.backflow-enabled", havingValue = "true", matchIfMissing = false)
public class DataBackflowScheduler {
    /**
     * RedisKey：用户反馈时间戳排序key，timeStamp:feedback_key
     */
    private static final String REDIS_KEY_FEEDBACK_SORTED = "ei:feedback:sorted";

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private ConversationManagementService conversationManagementService;

    /**
     * 数据回流定时任务
     */
    @Scheduled(fixedRateString = "${feedback.backflow-interval-ms}")
    @Async
    public void dataBackflowTask() {
        // 获取当前时间30分钟前的所有反馈数据key
        List<String> feedbackKeyList = redisClient.scoredSortedGet(REDIS_KEY_FEEDBACK_SORTED, Long.MIN_VALUE,
            System.currentTimeMillis() - (30 * 60 * 1000));
        for (String feedbackKey : feedbackKeyList) {
            FeedbackEntity feedback = JSON.parseObject(redisClient.get(feedbackKey), FeedbackEntity.class);
            if (feedback == null) {
                continue;
            }
            conversationManagementService.creatDataBackflowJob(feedback);
            redisClient.scoredSortedRemove(REDIS_KEY_FEEDBACK_SORTED, feedbackKey);
        }
    }

    /**
     * 数据回流数据清理定时任务
     */
    @Scheduled(cron = "${feedback.backflow-clear-cron-expression}")
    @Async
    public void dataBackflowClearTask() {
        // 清理1天前的所有反馈数据key
        redisClient.scoredSortedRemoveList(REDIS_KEY_FEEDBACK_SORTED, Long.MIN_VALUE,
            System.currentTimeMillis() - (24 * 60 * 60 * 1000L));
    }
}
