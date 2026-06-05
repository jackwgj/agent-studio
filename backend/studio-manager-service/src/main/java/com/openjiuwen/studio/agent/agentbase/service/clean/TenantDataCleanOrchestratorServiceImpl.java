/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean;

import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineContext;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineExecutor;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 租户数据清理编排服务实现类
 * 重构为使用管道模式（Strategy Pattern + Pipeline Pattern）
 * 采用策略模式 + 管道模式，将清理逻辑解耦为独立的、可插拔的任务
 *
 * @since 2025-12-17
 */
@Slf4j
@Service
public class TenantDataCleanOrchestratorServiceImpl implements TenantDataCleanOrchestratorService {

    private static final String LOG_CLEAN_ALL_START = "Starting tenant data cleanup for domainId: {}";

    private static final String LOG_CLEAN_ALL_SUCCESS = "Successfully cleaned tenant data for domainId: {}";

    private static final String LOG_CLEAN_ALL_FAILED = "Failed to clean tenant data for domainId: {}, error: {}";

    private final TenantCleanPipelineExecutor pipelineExecutor;

    @Autowired
    public TenantDataCleanOrchestratorServiceImpl(TenantCleanPipelineExecutor pipelineExecutor) {
        this.pipelineExecutor = pipelineExecutor;
    }

    /**
     * 执行租户数据清理
     * 采用管道模式按照固定顺序执行清理任务：
     * 1. TenantCleanKnowledgeBaseTask - 清理知识库内容和相关表
     * - 调用平台接口清理自建知识库
     * - 清理知识库相关数据表（t_knowledge_test_record等）
     * 2. TenantCleanThirdPartyKnowledgeBaseTask - 清理第三方知识库连接
     * - 清理 t_knowledge_base_connection 表
     * 3. TenantCleanKnowledgeBaseRouterTask - 清理知识库路由
     * - 清理 t_kb_connection_router 表
     *
     * @param domainId 租户ID
     */
    @Override
    @Async("tenantDataCleanTaskExecutor")
    public void executeClean(String domainId) {
        try {
            log.info(LOG_CLEAN_ALL_START, domainId);

            // 创建管道上下文
            TenantCleanPipelineContext context = new TenantCleanPipelineContext();

            context.setDomainId(domainId);

            // 执行管道清理流程
            pipelineExecutor.execute(context);

            log.info(LOG_CLEAN_ALL_SUCCESS, domainId);

        } catch (Exception e) {
            log.error(LOG_CLEAN_ALL_FAILED, domainId, e.getMessage());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }
}