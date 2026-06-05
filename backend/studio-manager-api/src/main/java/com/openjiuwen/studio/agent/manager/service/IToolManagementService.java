/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateToolOpenAPIResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateToolReq;
import com.openjiuwen.studio.agent.manager.dto.CreateToolRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateVersionReq;
import com.openjiuwen.studio.agent.manager.dto.GetToolVersionQo;
import com.openjiuwen.studio.agent.manager.dto.ListToolsV1Qo;
import com.openjiuwen.studio.agent.manager.dto.ModifyToolReq;
import com.openjiuwen.studio.agent.manager.dto.ModifyToolRsp;
import com.openjiuwen.studio.agent.manager.dto.Tool;
import com.openjiuwen.studio.agent.manager.dto.ToolCredential;
import com.openjiuwen.studio.agent.manager.dto.ToolListRsp;
import com.openjiuwen.studio.agent.manager.dto.VersionInfo;
import com.openjiuwen.studio.agent.manager.dto.VersionListRsp;

/**
 * ToolManagement service
 */

public interface IToolManagementService {

    /**
     * addToolCredential
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     * @param needValidate needValidate
     * @param body body
     */
    ToolCredential addToolCredential(String projectId, String toolId, String workspaceId, Boolean needValidate,
        ToolCredential body);

    /**
     * checkToolIsUsed
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    Boolean checkToolIsUsed(String projectId, String toolId, String workspaceId);

    /**
     * createToolOpenAPI
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateToolOpenAPIResponseBody createToolOpenAPI(String projectId, String workspaceId, CreateToolReq body);

    /**
     * createToolOpenAPIByIdV1
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    CreateToolOpenAPIResponseBody createToolOpenAPIByIdV1(String projectId, String toolId, String workspaceId);

    /**
     * createToolV1
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateToolRsp createToolV1(String projectId, String workspaceId, CreateToolReq body);

    /**
     * deleteToolCredential
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteToolCredential(String projectId, String toolId, String workspaceId);

    /**
     * deleteToolV1
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteToolV1(String projectId, String toolId, String workspaceId);

    /**
     * deleteToolVersion
     *
     * @param projectId projectId
     * @param versionId versionId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteToolVersion(String projectId, String versionId, String toolId, String workspaceId);

    /**
     * getToolVersion
     *
     * @param projectId projectId
     * @param versionId versionId
     * @param toolId toolId
     * @param getToolVersionQo getToolVersionQo
     */
    Tool getToolVersion(String projectId, String versionId, String toolId, GetToolVersionQo getToolVersionQo);

    /**
     * listToolVersions
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    VersionListRsp listToolVersions(String projectId, String toolId, String workspaceId);

    /**
     * listToolsV1
     *
     * @param projectId projectId
     * @param listToolsV1Qo listToolsV1Qo
     */
    ToolListRsp listToolsV1(String projectId, ListToolsV1Qo listToolsV1Qo);

    /**
     * modifyToolV1
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param toolId toolId
     * @param body body
     */
    ModifyToolRsp modifyToolV1(String projectId, String workspaceId, String toolId, ModifyToolReq body);

    /**
     * releaseToolVersion
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     * @param body body
     */
    VersionInfo releaseToolVersion(String projectId, String toolId, String workspaceId, CreateVersionReq body);

    /**
     * retrieveToolV1
     *
     * @param projectId projectId
     * @param toolId toolId
     * @param workspaceId workspaceId
     */
    Tool retrieveToolV1(String projectId, String toolId, String workspaceId);
}