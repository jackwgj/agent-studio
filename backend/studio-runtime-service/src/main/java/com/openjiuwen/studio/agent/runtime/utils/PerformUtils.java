/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 性能日志工具类
 *
 */
public class PerformUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PerformUtils.class);

    private static final Logger PERFORM_LOGGER = LoggerFactory.getLogger(PerformLogger.class);

    private static final String LOG_FORMAT = "{}|{}|{}|{}|{}|{}|{}|[{}]|{}|[{},{}]";

    /**
     * 记录时间
     *
     * @param executeParams 执行参数
     * @param key 记录事件key
     * @param time 记录事件值
     */
    public static void record(ExecuteParams executeParams, String key, Long time) {
        executeParams.getTimeRecord().put(key, time);
    }

    /**
     * 累计时延
     *
     * @param executeParams 执行参数
     * @param key 累计时延key
     * @param deltaTime 增加的时间
     */
    public static void accumulation(ExecuteParams executeParams, String key, Long deltaTime) {
        Long accTime = executeParams.getTimeRecord().computeIfAbsent(key, k -> 0L) + deltaTime;
        executeParams.getTimeRecord().put(key, accTime);
    }

    /**
     * 记录首事件时间
     *
     * @param executeParams 执行参数
     * @param key 累计时延key
     * @param time 时间
     */
    public static void recordFirstEvent(ExecuteParams executeParams, String key, Long time) {
        executeParams.getTimeRecord().computeIfAbsent(key, k -> time);
    }

    /**
     * 工作流时延统计
     *
     * @param executeParams 工作流执行参数
     * @return
     */
    public static void workflowLog(ExecuteParams executeParams) {
        try {
            long start = executeParams.getStartTime();
            long end = executeParams.getTimeRecord().get(Constant.Performance.END);

            long engineStart = executeParams.getTimeRecord().get(Constant.Performance.ENGINE_START);
            long engineEnd = executeParams.getTimeRecord().get(Constant.Performance.ENGINE_END);

            long firstEvent = executeParams.getTimeRecord().get(Constant.Performance.FIRST_EVENT);

            // 总耗时
            long total = end - start;

            // engine总耗时
            long inEngine = engineEnd - engineStart;

            // before_engine耗时
            long beforeEngine = engineStart - start;

            // engine内manager耗时
            long managerTime = executeParams.getTimeRecord().get(Constant.Performance.MANAGER);

            // 首事件耗时
            long firstEventTime = firstEvent - start;

            // IR加载耗时
            long loadIRTime = executeParams.getTimeRecord().get(Constant.Performance.LOAD_IR);

            // 请求引擎到收到第一条请求
            long enginFirstTime = firstEvent - engineStart;

            PERFORM_LOGGER.info(LOG_FORMAT, Constant.AppType.WORKFLOW, executeParams.getWorkflowId(),
                executeParams.getConversationId(), executeParams.getSuccess(), total, firstEventTime, beforeEngine,
                loadIRTime, inEngine, enginFirstTime, managerTime);
        } catch (Exception e) {
            LOG.error("statistic time failed.", e);
            PERFORM_LOGGER.info(LOG_FORMAT, Constant.AppType.WORKFLOW, executeParams.getConversationId(),
                executeParams.getWorkflowId(), -1, -1, -1, -1, -1, -1, -1, -1);
        }
    }

    /**
     * 记录Agent/控制器时间
     *
     * @param executeParams 执行参数
     * @param key 记录事件key
     * @param time 记录事件值
     */
    public static void recordAgent(AgentExecuteParams executeParams, String key, Long time) {
        executeParams.getTimeRecord().put(key, time);
    }

    /**
     * 累计时延
     *
     * @param executeParams 执行参数
     * @param key 累计时延key
     * @param deltaTime 增加的时间
     */
    public static void accumulationAgent(AgentExecuteParams executeParams, String key, Long deltaTime) {
        Long accTime = executeParams.getTimeRecord().computeIfAbsent(key, k -> 0L) + deltaTime;
        executeParams.getTimeRecord().put(key, accTime);
    }

    /**
     * 记录Agent/控制器首事件时间
     *
     * @param executeParams 执行参数
     * @param key 累计时延key
     * @param time 时间
     */
    public static void recordFirstAgentEvent(AgentExecuteParams executeParams, String key, Long time) {
        executeParams.getTimeRecord().computeIfAbsent(key, k -> time);
    }

    /**
     * Agent/控制器时延统计
     *
     * @param executeParams Agent/控制器执行参数
     * @return
     */
    public static void agentLog(AgentExecuteParams executeParams) {
        try {
            long start = executeParams.getStartTime();
            long end = executeParams.getTimeRecord().get(Constant.Performance.END);

            long engineStart = executeParams.getTimeRecord().get(Constant.Performance.ENGINE_START);
            long engineEnd = executeParams.getTimeRecord().get(Constant.Performance.ENGINE_END);

            long firstEvent = executeParams.getTimeRecord().get(Constant.Performance.FIRST_EVENT);

            // 总耗时
            long total = end - start;

            // engine总耗时
            long inEngine = engineEnd - engineStart;

            // before_engine耗时
            long beforeEngine = engineStart - start;

            // IR加载耗时
            long loadIrTime = executeParams.getTimeRecord().get(Constant.Performance.LOAD_IR);

            // engine内manager耗时
            long managerTime = executeParams.getTimeRecord().get(Constant.Performance.MANAGER);

            // 首事件耗时
            long firstEventTime = firstEvent - start;

            // 请求引擎到收到第一条请求耗时
            long enginFirstTime = firstEvent - engineStart;

            PERFORM_LOGGER.info(LOG_FORMAT, executeParams.getExecuteType(), executeParams.getAgentId(),
                executeParams.getConversationId(), executeParams.getSuccess(), total, firstEventTime, beforeEngine,
                loadIrTime, inEngine, enginFirstTime, managerTime);
        } catch (Exception e) {
            LOG.error("statistic time failed.", e);
            PERFORM_LOGGER.info(LOG_FORMAT, executeParams.getExecuteType(), executeParams.getConversationId(),
                executeParams.getAgentId(), -1, -1, -1, -1, -1, -1, -1, -1);
        }
    }
}
