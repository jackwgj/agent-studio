/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KnowledgeBaseDatasetResourceUsageQueryService implements ResourceUsageQueryService {

    private final KnowledgeRepoMapper knowledgeRepoMapper;

    public KnowledgeBaseDatasetResourceUsageQueryService(KnowledgeRepoMapper knowledgeRepoMapper) {
        this.knowledgeRepoMapper = knowledgeRepoMapper;

    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.KNOWLEDGE_BASE_DATESET;
    }

    @Override
    public Long queryResourceUsageCount(String domainId) {
        log.error("can not support current method");
        throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "knowledgeBaseId");
    }

    @Override
    public Long queryResourceUsageCountInKnowledgeBase(String domainId, String knowledgeBaseId) {
        KnowledgeRepoEntity knowledgeRepoEntity = knowledgeRepoMapper.selectByPrimaryKey(knowledgeBaseId, null);
        if (knowledgeRepoEntity == null) {
            return 0L;
        }
        return (long) knowledgeRepoEntity.getFileNum();
    }

}
