/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import com.openjiuwen.studio.agent.agentbase.common.enums.KnowledgeBaseType;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KnowledgeBaseResourceUsageQueryService implements ResourceUsageQueryService {
    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public KnowledgeBaseResourceUsageQueryService(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.KNOWLEDGE_BASE;
    }

    @Override
    public Long queryResourceUsageCount(String domainId) {
        return (long) knowledgeBaseMapper.countByType(KnowledgeBaseType.INTERNAL.getValue(), domainId).size();
    }

    @Override
    public Long queryResourceUsageCountInKnowledgeBase(String domainId, String knowledgeBaseId) {
        log.error("do not support this query");
        throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
    }

}
