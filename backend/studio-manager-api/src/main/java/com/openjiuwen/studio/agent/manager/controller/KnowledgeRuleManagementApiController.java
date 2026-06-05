/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeSegmentRuleRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeSegmentRuleResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteOneResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeSegmentRulesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeSegmentRuleRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeSegmentRuleResponseBody;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeRuleManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * KnowledgeRuleManagement controller
 */
@RestController

public class KnowledgeRuleManagementApiController implements KnowledgeRuleManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeRuleManagementApiController.class);

    @Autowired
    private IKnowledgeRuleManagementService knowledgeRuleManagementService;

    @Override
    public ResponseEntity<CreateKnowledgeSegmentRuleResponseBody> createKnowledgeSegmentRule(String projectId,
        String workspaceId, CreateKnowledgeSegmentRuleRequestBody body) {
        return ResponseModel.success(
            knowledgeRuleManagementService.createKnowledgeSegmentRule(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<DeleteOneResponseBody> deleteKnowledgeSegmentRule(String projectId, String id,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeRuleManagementService.deleteKnowledgeSegmentRule(projectId, id, workspaceId));
    }

    @Override
    public ResponseEntity<ListKnowledgeSegmentRulesResponseBody> listKnowledgeSegmentRules(String projectId,
        String workspaceId, String repoId) {
        return ResponseModel.success(
            knowledgeRuleManagementService.listKnowledgeSegmentRules(projectId, workspaceId, repoId));
    }

    @Override
    public ResponseEntity<ModifyKnowledgeSegmentRuleResponseBody> modifyKnowledgeSegmentRule(String projectId,
        String workspaceId, String id, ModifyKnowledgeSegmentRuleRequestBody body) {
        return ResponseModel.success(
            knowledgeRuleManagementService.modifyKnowledgeSegmentRule(projectId, workspaceId, id, body));
    }
}