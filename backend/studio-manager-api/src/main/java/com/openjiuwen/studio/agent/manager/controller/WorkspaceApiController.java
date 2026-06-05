/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CreateWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceListRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceQo;
import com.openjiuwen.studio.agent.manager.dto.UpdateWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;
import com.openjiuwen.studio.agent.manager.service.IWorkspaceService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workspace controller
 */
@RestController

public class WorkspaceApiController implements WorkspaceApi {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceApiController.class);

    @Autowired
    private IWorkspaceService workspaceService;

    @Override
    public ResponseEntity<String> createWorkspace(String projectId, CreateWorkspaceReq body) {
        return ResponseModel.success(workspaceService.createWorkspace(projectId, body));
    }

    @Override
    public ResponseEntity<String> deleteWorkspace(String projectId, DeleteWorkspaceReq body) {
        return ResponseModel.success(workspaceService.deleteWorkspace(projectId, body));
    }

    @Override
    public ResponseEntity<GetWorkspaceListRsp> initWorkspace(String projectId, String scope) {
        return ResponseModel.success(workspaceService.initWorkspace(projectId, scope));
    }

    @Override
    public ResponseEntity<GetWorkspaceListRsp> queryWorkspace(String projectId, QueryWorkspaceQo queryWorkspaceQo) {
        return ResponseModel.success(workspaceService.queryWorkspace(projectId, queryWorkspaceQo));
    }

    @Override
    public ResponseEntity<WorkspaceInfo> queryWorkspaceById(String projectId, String workspaceId) {
        return ResponseModel.success(workspaceService.queryWorkspaceById(projectId, workspaceId));
    }

    @Override
    public ResponseEntity<WorkspaceInfo> updateWorkspace(String projectId, UpdateWorkspaceReq body) {
        return ResponseModel.success(workspaceService.updateWorkspace(projectId, body));
    }
}