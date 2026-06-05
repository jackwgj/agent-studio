/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeShareScopeEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ShareScopeEnum;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeShareScopeMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ShareScopeValidUtil {

    private static KnowledgeShareScopeMapper knowledgeShareScopeMapper;

    public ShareScopeValidUtil(KnowledgeShareScopeMapper knowledgeShareScopeMapper) {
        ShareScopeValidUtil.knowledgeShareScopeMapper = knowledgeShareScopeMapper;
    }

    public static void checkKnowledgeScope(String knowledgeBaseId, String workspaceId,
        KnowledgeBaseEntity knowledgeBaseEntity) {
        ShareScopeEnum scopeEnum = ShareScopeEnum.valueOf(
            Optional.ofNullable(knowledgeBaseEntity.getShareScope()).orElse(ShareScopeEnum.SELF.toString()));
        switch (scopeEnum) {
            case GLOBAL:
                return;
            case SELF:
                if (!knowledgeBaseEntity.getWorkspaceId().equals(workspaceId)) {
                    log.error("User does not have permission to view knowledge base:{} details.", knowledgeBaseId);
                    throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST);
                }
                break;
            case PARTIAL:
                KnowledgeShareScopeEntity knowledgeShareScopeEntity = knowledgeShareScopeMapper.queryShareScope(
                    knowledgeBaseId, workspaceId);
                if (ObjectUtils.isNotEmpty(knowledgeShareScopeEntity) && !knowledgeShareScopeEntity.getWorkspaceId()
                    .equals(workspaceId)) {
                    log.error("User does not have permission to view knowledge base:{} details.", knowledgeBaseId);
                    throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST);
                }
                break;
        }
    }
}
