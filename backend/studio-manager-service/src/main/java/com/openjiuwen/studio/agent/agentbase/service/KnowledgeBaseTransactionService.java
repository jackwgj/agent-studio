/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2B;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class KnowledgeBaseTransactionService {

    private final ResourceValidateService resourceValidateService;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeRepoMapper knowledgeRepoMapper;

    public KnowledgeBaseTransactionService(ResourceValidateService resourceValidateService,
        KnowledgeBaseMapper knowledgeBaseMapper, KnowledgeRepoMapper knowledgeRepoMapper) {
        this.resourceValidateService = resourceValidateService;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeRepoMapper = knowledgeRepoMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createKnowledgeBaseInTransaction(KnowledgeRepoEntity repoEntity, KnowledgeBaseEntity baseEntity) {
        if (!TENANT_TYPE_2B.equals(TenantTypeUtil.getTenantType())) {
            resourceValidateService.resourceQuotaCheck(baseEntity.getDomainId(), ResourceType.KNOWLEDGE_BASE, 1);
        }
        knowledgeRepoMapper.insert(repoEntity);
        knowledgeBaseMapper.insertKnowledgeBase(baseEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearWhenCreateFailed(String domainId, KnowledgeRepoEntity knowledgeRepoEntity,
        KnowledgeBaseEntity knowledgeBaseEntity) {
        knowledgeRepoMapper.deleteByPrimaryKey(knowledgeRepoEntity.getKnowledgeRepoId(),
            knowledgeRepoEntity.getProjectId());
        knowledgeBaseMapper.batchDeleteKnowledgeBase(knowledgeBaseEntity.getProjectId(),
            Collections.singletonList(knowledgeBaseEntity.getId()));
    }
}
