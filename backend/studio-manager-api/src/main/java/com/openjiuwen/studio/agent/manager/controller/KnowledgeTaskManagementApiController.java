/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeTaskReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksResponseBody;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeTaskManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * KnowledgeTaskManagement controller
 */
@RestController

public class KnowledgeTaskManagementApiController implements KnowledgeTaskManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeTaskManagementApiController.class);

    @Autowired
    private IKnowledgeTaskManagementService knowledgeTaskManagementService;

    @Override
    public ResponseEntity<CreateKnowledgeTaskResponseBody> createKnowledgeTask(String projectId, String workspaceId,
        String knowledgeRepoId, CreateKnowledgeTaskRequestBody body) {
        return ResponseModel.success(
            knowledgeTaskManagementService.createKnowledgeTask(projectId, workspaceId, knowledgeRepoId, body));
    }

    @Override
    public ResponseEntity<CommonBatchDeleteRsp> deleteKnowledgeTask(String projectId, String knowledgeRepoId,
        String workspaceId, DeleteKnowledgeTaskReq body) {
        return ResponseModel.success(
            knowledgeTaskManagementService.deleteKnowledgeTask(projectId, knowledgeRepoId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ListKnowledgeTasksResponseBody> listKnowledgeTasks(String projectId, String knowledgeRepoId,
        ListKnowledgeTasksQo listKnowledgeTasksQo) {
        return ResponseModel.success(
            knowledgeTaskManagementService.listKnowledgeTasks(projectId, knowledgeRepoId, listKnowledgeTasksQo));
    }
}