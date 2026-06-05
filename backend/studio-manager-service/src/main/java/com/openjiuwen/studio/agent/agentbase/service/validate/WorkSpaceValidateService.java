/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.validate;

import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WorkSpaceValidateService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public WorkSpaceValidateService(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    public boolean validateKnowledgeBaseForWorkspace(String knowledgeBaseId, String workspaceId) {
        return Objects.nonNull(knowledgeBaseMapper.selectByIdAndWorkspaceId(workspaceId, knowledgeBaseId));
    }
}

