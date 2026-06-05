/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqReq;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqResp;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateFaqReq;
import com.openjiuwen.studio.agent.manager.dto.CreateFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ListFaqQo;
import com.openjiuwen.studio.agent.manager.dto.ListFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ModifyFaqReq;
import com.openjiuwen.studio.agent.manager.dto.ModifyFaqResp;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeFaqManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * KnowledgeFaqManagement controller
 */
@RestController

public class KnowledgeFaqManagementApiController implements KnowledgeFaqManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeFaqManagementApiController.class);

    @Autowired
    private IKnowledgeFaqManagementService knowledgeFaqManagementService;

    @Override
    public ResponseEntity<BatchDeleteFaqResp> batchDeleteFaq(String projectId, String knowledgeRepoId,
        String workspaceId, BatchDeleteFaqReq body) {
        return ResponseModel.success(
            knowledgeFaqManagementService.batchDeleteFaq(projectId, knowledgeRepoId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CreateFaqResp> createFaq(String projectId, String workspaceId, String knowledgeRepoId,
        CreateFaqReq body) {
        return ResponseModel.success(
            knowledgeFaqManagementService.createFaq(projectId, workspaceId, knowledgeRepoId, body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteFaq(String projectId, String knowledgeRepoId, String faqId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeFaqManagementService.deleteFaq(projectId, knowledgeRepoId, faqId, workspaceId));
    }

    @Override
    public ResponseEntity<ListFaqResp> listFaq(String projectId, String knowledgeRepoId, ListFaqQo listFaqQo) {
        return ResponseModel.success(knowledgeFaqManagementService.listFaq(projectId, knowledgeRepoId, listFaqQo));
    }

    @Override
    public ResponseEntity<ModifyFaqResp> modifyFaq(String projectId, String workspaceId, String knowledgeRepoId,
        String faqId, ModifyFaqReq body) {
        return ResponseModel.success(
            knowledgeFaqManagementService.modifyFaq(projectId, workspaceId, knowledgeRepoId, faqId, body));
    }
}