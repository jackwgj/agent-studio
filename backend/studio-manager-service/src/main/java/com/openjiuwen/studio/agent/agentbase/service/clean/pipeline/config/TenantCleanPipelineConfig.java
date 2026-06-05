/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.config;

import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineTask;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task.TenantCleanKnowledgeBaseRouterTask;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task.TenantCleanKnowledgeBaseTask;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task.TenantCleanThirdPartyKnowledgeBaseTask;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 租户清理管道配置类
 * <p>
 * 使用显式编排来定义任务的执行顺序，而不是依赖 @Order 注解
 * 这样可以在一个地方清晰地看到整个清理流程的顺序
 *
 * @since 2025-12-20
 */
@Configuration
public class TenantCleanPipelineConfig {

    /**
     * 定义租户知识库清理的任务执行顺序
     * <p>
     * 执行逻辑：
     * 1. TenantCleanKnowledgeBaseTask - 清理知识库内容和相关表
     * - 先调用平台接口清理自建知识库
     * - 然后清理知识库相关数据表
     * 2. TenantCleanThirdPartyKnowledgeBaseTask - 清理第三方知识库连接
     * - 清理 t_knowledge_base_connection 表
     * 3. TenantCleanKnowledgeBaseRouterTask - 清理知识库路由
     * - 清理 t_kb_connection_router 表
     * <p>
     * 注意：采用显式编排模式，执行顺序由 List 顺序决定，@Order 注解将被忽略
     * 这种顺序与原来的 TenantDataCleanOrchestratorServiceImpl 保持一致
     *
     * @param knowledgeBaseTask 知识库清理任务
     * @param thirdPartyTask 第三方知识库清理任务
     * @param routerTask 知识库路由清理任务
     * @return 按顺序排列的任务列表
     */
    @Bean("tenantKnowledgeCleanPipeline")
    public List<TenantCleanPipelineTask> tenantKnowledgeCleanPipeline(TenantCleanKnowledgeBaseTask knowledgeBaseTask,
        TenantCleanThirdPartyKnowledgeBaseTask thirdPartyTask, TenantCleanKnowledgeBaseRouterTask routerTask) {

        // 这里的 List 顺序就是执行顺序，完全忽略 @Order 注解
        return Arrays.asList(knowledgeBaseTask,      // 1. 清理知识库内容和相关表
            thirdPartyTask,         // 2. 清理第三方知识库连接
            routerTask              // 3. 清理知识库路由
        );
    }

    /**
     * 可以定义其他清理管道，比如：
     * - 租户整体清理管道（包含知识库清理 + 其他清理）
     * - 仅表数据清理管道（跳过平台接口调用）
     * - 条件清理管道（满足特定条件才执行）
     */
}