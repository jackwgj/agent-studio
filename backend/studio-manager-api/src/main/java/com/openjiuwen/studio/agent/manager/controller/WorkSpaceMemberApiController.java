/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CommonResponse;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberListRsp;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberRoleRsp;
import com.openjiuwen.studio.agent.manager.dto.MemberOwnershipBody;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceMemberListQo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody1;
import com.openjiuwen.studio.agent.manager.service.IWorkSpaceMemberService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * WorkSpaceMember controller
 */
@RestController

public class WorkSpaceMemberApiController implements WorkSpaceMemberApi {
    private static final Logger log = LoggerFactory.getLogger(WorkSpaceMemberApiController.class);

    @Autowired
    private IWorkSpaceMemberService workSpaceMemberService;

    @Override
    public ResponseEntity<Integer> batchAddWorkspaceMember(String projectId, String workspaceId,
        WorkspaceMemberBody body) {
        return ResponseModel.success(workSpaceMemberService.batchAddWorkspaceMember(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Integer> batchDeleteWorkspaceMember(String projectId, String workspaceId,
        DeleteWorkspaceMemberReq body) {
        return ResponseModel.success(workSpaceMemberService.batchDeleteWorkspaceMember(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Integer> batchUpdateWorkspaceMemberRole(String workspaceId, String projectId,
        WorkspaceMemberBody1 body) {
        return ResponseModel.success(
            workSpaceMemberService.batchUpdateWorkspaceMemberRole(workspaceId, projectId, body));
    }

    @Override
    public ResponseEntity<CommonResponse> exitWorkspace(String projectId, String workspaceId) {
        return ResponseModel.success(workSpaceMemberService.exitWorkspace(projectId, workspaceId));
    }

    @Override
    public ResponseEntity<GetWorkspaceMemberListRsp> queryIamUserList(String projectId, String workspaceId) {
        return ResponseModel.success(workSpaceMemberService.queryIamUserList(projectId));
    }

    @Override
    public ResponseEntity<GetWorkspaceMemberRoleRsp> queryRoleList(String projectId, String workspaceId) {
        return ResponseModel.success(workSpaceMemberService.queryRoleList(projectId));
    }

    @Override
    public ResponseEntity<GetWorkspaceMemberListRsp> queryWorkspaceMemberList(String projectId,
        QueryWorkspaceMemberListQo queryWorkspaceMemberListQo) {
        return ResponseModel.success(
            workSpaceMemberService.queryWorkspaceMemberList(projectId, queryWorkspaceMemberListQo));
    }

    @Override
    public ResponseEntity<CommonResponse> transferWorkspaceOwnership(String projectId, String workspaceId, MemberOwnershipBody body) {
        return ResponseModel.success(workSpaceMemberService.transferWorkspaceOwnership(projectId, body));
    }
}