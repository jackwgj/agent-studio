/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThirdPartyKnowledgeBaseConnectionResourceUsageQueryService implements ResourceUsageQueryService {

    private final KnowledgeBaseConnectionMapper connectionMapper;

    public ThirdPartyKnowledgeBaseConnectionResourceUsageQueryService(KnowledgeBaseConnectionMapper connectionMapper) {
        this.connectionMapper = connectionMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.THIRD_PARTY_KNOWLEDGE_BASE_CONNECTION;
    }

    @Override
    public Long queryResourceUsageCount(String domainId) {
        return connectionMapper.countByDomainId(domainId);
    }

    @Override
    public Long queryResourceUsageCountInKnowledgeBase(String domainId, String knowledgeBaseId) {
        log.error("do not support this query");
        throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
    }
}
