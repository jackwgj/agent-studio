/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeSegmentRuleRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeSegmentRuleResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteOneResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeSegmentRulesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeSegmentRuleRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeSegmentRuleResponseBody;

/**
 * KnowledgeRuleManagement service
 */

public interface IKnowledgeRuleManagementService {

    /**
     * createKnowledgeSegmentRule
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateKnowledgeSegmentRuleResponseBody createKnowledgeSegmentRule(String projectId, String workspaceId,
        CreateKnowledgeSegmentRuleRequestBody body);

    /**
     * deleteKnowledgeSegmentRule
     *
     * @param projectId projectId
     * @param id id
     * @param workspaceId workspaceId
     */
    DeleteOneResponseBody deleteKnowledgeSegmentRule(String projectId, String id, String workspaceId);

    /**
     * listKnowledgeSegmentRules
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param repoId repoId
     */
    ListKnowledgeSegmentRulesResponseBody listKnowledgeSegmentRules(String projectId, String workspaceId,
        String repoId);

    /**
     * modifyKnowledgeSegmentRule
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param id id
     * @param body body
     */
    ModifyKnowledgeSegmentRuleResponseBody modifyKnowledgeSegmentRule(String projectId, String workspaceId, String id,
        ModifyKnowledgeSegmentRuleRequestBody body);
}