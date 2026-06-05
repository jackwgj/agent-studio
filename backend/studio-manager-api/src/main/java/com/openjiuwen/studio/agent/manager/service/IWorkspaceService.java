/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CreateWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.GetWorkspaceListRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryWorkspaceQo;
import com.openjiuwen.studio.agent.manager.dto.UpdateWorkspaceReq;
import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;

/**
 * Workspace service
 */

public interface IWorkspaceService {

    /**
     * createWorkspace
     *
     * @param projectId projectId
     * @param body body
     */
    String createWorkspace(String projectId, CreateWorkspaceReq body);

    /**
     * deleteWorkspace
     *
     * @param projectId projectId
     * @param body body
     */
    String deleteWorkspace(String projectId, DeleteWorkspaceReq body);

    /**
     * initWorkspace
     *
     * @param projectId projectId
     * @param scope scope
     */
    GetWorkspaceListRsp initWorkspace(String projectId, String scope);

    /**
     * queryWorkspace
     *
     * @param projectId projectId
     * @param queryWorkspaceQo queryWorkspaceQo
     */
    GetWorkspaceListRsp queryWorkspace(String projectId, QueryWorkspaceQo queryWorkspaceQo);

    /**
     * queryWorkspaceById
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    WorkspaceInfo queryWorkspaceById(String projectId, String workspaceId);

    /**
     * updateWorkspace
     *
     * @param projectId projectId
     * @param body body
     */
    WorkspaceInfo updateWorkspace(String projectId, UpdateWorkspaceReq body);
}