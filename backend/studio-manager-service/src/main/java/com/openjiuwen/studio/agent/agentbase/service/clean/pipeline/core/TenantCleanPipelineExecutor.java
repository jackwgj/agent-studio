/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 租户清理管道执行器
 * <p>
 * 负责执行所有注册的管道任务，并控制执行流程
 *
 * @since 2025-12-20
 */
@Slf4j
@Service
public class TenantCleanPipelineExecutor {

    private static final String LOG_PIPELINE_START = "Starting tenant clean pipeline for domainId: {}";

    private static final String LOG_PIPELINE_SUCCESS = "Tenant clean pipeline completed successfully for domainId: {}";

    private static final String LOG_PIPELINE_FAILED = "Tenant clean pipeline failed for domainId: {}, error: {}";

    private static final String LOG_TASK_START = "Starting task: {} (Order: {})";

    private static final String LOG_TASK_SUCCESS = "Task completed successfully: {}";

    private static final String LOG_TASK_FAILED = "Task failed: {}, error: {}";

    private static final String LOG_TASK_SKIPPED = "Task skipped: {}";

    /**
     * 注入配置类中显式编排好的任务列表
     * 采用显式编排模式，由 TenantCleanPipelineConfig 定义执行顺序
     * 此时 @Order 注解将被忽略，顺序完全由 Config 中的 List 顺序决定
     */
    @Resource(name = "tenantKnowledgeCleanPipeline")
    private List<TenantCleanPipelineTask> pipelineTasks;

    /**
     * 执行管道流程
     *
     * @param context 管道上下文
     * @return 执行后的上下文
     */
    public TenantCleanPipelineContext execute(TenantCleanPipelineContext context) {
        try {
            log.info(LOG_PIPELINE_START, context.getDomainId());

            // 1. 过滤出需要执行的任务（显式编排的顺序已经是正确的）
            List<TenantCleanPipelineTask> tasksToExecute = Optional.ofNullable(pipelineTasks)
                .orElse(Collections.emptyList()) // 如果是 null，就用空列表代替
                .stream()
                .filter(task -> task.shouldExecute(context))
                .collect(Collectors.toList());

            log.debug("Found {} tasks to execute for domainId: {}", tasksToExecute.size(), context.getDomainId());

            // 2. 按顺序执行任务
            for (TenantCleanPipelineTask task : tasksToExecute) {
                // 检查是否发生过中断
                if (context.isStopped()) {
                    log.warn("--- Pipeline stopped: {} ---", context.getStopReason());
                    break;
                }

                // 执行任务
                executeTask(task, context);
            }

            log.info(LOG_PIPELINE_SUCCESS, context.getDomainId());
            return context;

        } catch (Exception e) {
            log.error(LOG_PIPELINE_FAILED, context.getDomainId(), e.getMessage());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }

    /**
     * 执行单个任务
     *
     * @param task 任务
     * @param context 上下文
     */
    private void executeTask(TenantCleanPipelineTask task, TenantCleanPipelineContext context) {
        try {
            log.debug(LOG_TASK_START, task.getName(), task.getOrder());

            task.execute(context);

            log.debug(LOG_TASK_SUCCESS, task.getName());

        } catch (Exception e) {
            log.error(LOG_TASK_FAILED, task.getName(), e.getMessage());

            // 调用失败处理逻辑
            task.onFailed(context, e);

            // 终止整个管道流程
            context.stop("Task execution failed: " + task.getName());
        }
    }
}
