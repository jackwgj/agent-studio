/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2C;

import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KnowledgeBaseDatasetStorageResourceUsageQueryService implements ResourceUsageQueryService {

    private final KnowledgeRepoMapper knowledgeRepoMapper;

    public KnowledgeBaseDatasetStorageResourceUsageQueryService(KnowledgeRepoMapper knowledgeRepoMapper) {
        this.knowledgeRepoMapper = knowledgeRepoMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.KNOWLEDGE_BASE_DATESET_STORAGE;
    }

    @Override
    public Long queryResourceUsageCount(String domainId) {
        Long count = knowledgeRepoMapper.sumKnowledgeRepoSizeWithTenantType(domainId, TENANT_TYPE_2C);
        return count == null ? 0L : count;
    }

    @Override
    public Long queryResourceUsageCountInKnowledgeBase(String domainId, String knowledgeBaseId) {
        log.error("do not query fileSize used in single knowledge base");
        throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
    }

}
