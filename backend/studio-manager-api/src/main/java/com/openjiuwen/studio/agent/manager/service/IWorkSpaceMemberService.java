/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CommonResponse;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceMemberReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberListRsp;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceMemberRoleRsp;
import com.openjiuwen.studio.agent.manager.dto.MemberOwnershipBody;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceMemberListQo;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceMemberBody1;

/**
 * WorkSpaceMember service
 */

public interface IWorkSpaceMemberService {

    /**
     * batchAddWorkspaceMember
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    Integer batchAddWorkspaceMember(String projectId, String workspaceId, WorkspaceMemberBody body);

    /**
     * batchDeleteWorkspaceMember
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    Integer batchDeleteWorkspaceMember(String projectId, String workspaceId, DeleteWorkspaceMemberReq body);

    /**
     * batchUpdateWorkspaceMemberRole
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param body body
     */
    Integer batchUpdateWorkspaceMemberRole(String workspaceId, String projectId, WorkspaceMemberBody1 body);

    /**
     * exitWorkspace
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    CommonResponse exitWorkspace(String projectId, String workspaceId);

    /**
     * queryIamUserList
     *
     * @param projectId projectId
     */
    GetWorkspaceMemberListRsp queryIamUserList(String projectId);

    /**
     * queryRoleList
     *
     * @param projectId projectId
     */
    GetWorkspaceMemberRoleRsp queryRoleList(String projectId);

    /**
     * queryWorkspaceMemberList
     *
     * @param projectId projectId
     * @param queryWorkspaceMemberListQo queryWorkspaceMemberListQo
     */
    GetWorkspaceMemberListRsp queryWorkspaceMemberList(String projectId,
        QueryWorkspaceMemberListQo queryWorkspaceMemberListQo);

    /**
     * transferWorkspaceOwnership
     *
     * @param projectId projectId
     * @param body body
     */
    CommonResponse transferWorkspaceOwnership(String projectId, MemberOwnershipBody body);
}