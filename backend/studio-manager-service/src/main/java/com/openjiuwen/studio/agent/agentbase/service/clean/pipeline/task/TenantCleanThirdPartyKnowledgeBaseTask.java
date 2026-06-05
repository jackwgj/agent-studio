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
 * 租户第三方知识库清理任务
 * <p>
 * 负责清理第三方知识库连接表 t_knowledge_base_connection
 *
 * @since 2025-12-20
 */
@Slf4j
@Component
@Order(20) // 在知识库清理(10)之后，路由(30)之前执行
public class TenantCleanThirdPartyKnowledgeBaseTask implements TenantCleanPipelineTask {

    private static final String LOG_CLEAN_THIRD_PARTY_START
        = "Starting third party knowledge base connection cleanup for domainId: {}";

    private static final String LOG_CLEAN_THIRD_PARTY_SUCCESS
        = "Successfully cleaned third party knowledge base connection for domainId: {}";

    private static final String LOG_CLEAN_THIRD_PARTY_FAILED
        = "Failed to clean third party knowledge base connection for domainId: {}, error: {}";

    private final CommonCleanMapper commonCleanMapper;

    public TenantCleanThirdPartyKnowledgeBaseTask(CommonCleanMapper commonCleanMapper) {
        this.commonCleanMapper = commonCleanMapper;
    }

    @Override
    public void execute(TenantCleanPipelineContext context) {
        try {
            String domainId = context.getDomainId();
            log.info(LOG_CLEAN_THIRD_PARTY_START, domainId);

            // 清理第三方知识库连接表数据
            int deletedRows = commonCleanMapper.deleteDataByDomainId("t_knowledge_base_connection", domainId);
            log.debug("Deleted {} rows from t_knowledge_base_connection for domainId: {}", deletedRows, domainId);

            log.info(LOG_CLEAN_THIRD_PARTY_SUCCESS, domainId);

        } catch (Exception e) {
            log.error(LOG_CLEAN_THIRD_PARTY_FAILED, context.getDomainId(), e.getMessage());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public String getName() {
        return "ThirdPartyKnowledgeBaseCleanup";
    }
}