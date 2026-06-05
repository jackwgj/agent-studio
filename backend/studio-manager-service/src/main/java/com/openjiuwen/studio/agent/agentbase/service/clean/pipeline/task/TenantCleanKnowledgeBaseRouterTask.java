/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task;

import com.openjiuwen.studio.agent.agentbase.mapper.CommonCleanMapper;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineContext;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineTask;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 租户知识库路由清理任务
 * <p>
 * 负责清理知识库连接路由表 t_kb_connection_router
 *
 * @since 2025-12-20
 */
@Slf4j
@Component
@Order(30) // 最后执行知识库路由清理
public class TenantCleanKnowledgeBaseRouterTask implements TenantCleanPipelineTask {

    private static final String LOG_CLEAN_ROUTER_START = "Starting knowledge base router cleanup for domainId: {}";

    private static final String LOG_CLEAN_ROUTER_SUCCESS
        = "Successfully cleaned knowledge base router for domainId: {}";

    private static final String LOG_CLEAN_ROUTER_FAILED
        = "Failed to clean knowledge base router for domainId: {}, error: {}";

    private final CommonCleanMapper commonCleanMapper;

    public TenantCleanKnowledgeBaseRouterTask(CommonCleanMapper commonCleanMapper) {
        this.commonCleanMapper = commonCleanMapper;
    }

    @Override
    public void execute(TenantCleanPipelineContext context) {
        try {
            String domainId = context.getDomainId();
            log.info(LOG_CLEAN_ROUTER_START, domainId);

            // 清理知识库路由表数据
            int deletedRows = commonCleanMapper.deleteDataByDomainId("t_kb_connection_router", domainId);
            log.debug("Deleted {} rows from t_kb_connection_router for domainId: {}", deletedRows, domainId);

            log.info(LOG_CLEAN_ROUTER_SUCCESS, domainId);

        } catch (Exception e) {
            log.error(LOG_CLEAN_ROUTER_FAILED, context.getDomainId(), e.getMessage());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public String getName() {
        return "KnowledgeBaseRouterCleanup";
    }
}