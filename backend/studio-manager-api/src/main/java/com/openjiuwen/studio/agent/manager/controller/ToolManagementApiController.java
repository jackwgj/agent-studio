/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

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
import com.openjiuwen.studio.agent.manager.service.IToolManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ToolManagement controller
 */
@RestController

public class ToolManagementApiController implements ToolManagementApi {
    private static final Logger log = LoggerFactory.getLogger(ToolManagementApiController.class);

    @Autowired
    private IToolManagementService toolManagementService;

    @Override
    public ResponseEntity<ToolCredential> addToolCredential(String projectId, String toolId, String workspaceId,
        Boolean needValidate, ToolCredential body) {
        return ResponseModel.success(
            toolManagementService.addToolCredential(projectId, toolId, workspaceId, needValidate, body));
    }

    @Override
    public ResponseEntity<Boolean> checkToolIsUsed(String projectId, String toolId, String workspaceId) {
        return ResponseModel.success(toolManagementService.checkToolIsUsed(projectId, toolId, workspaceId));
    }

    @Override
    public ResponseEntity<CreateToolOpenAPIResponseBody> createToolOpenAPI(String projectId, String workspaceId,
        CreateToolReq body) {
        return ResponseModel.success(toolManagementService.createToolOpenAPI(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CreateToolOpenAPIResponseBody> createToolOpenAPIByIdV1(String projectId, String toolId,
        String workspaceId) {
        return ResponseModel.success(toolManagementService.createToolOpenAPIByIdV1(projectId, toolId, workspaceId));
    }

    @Override
    public ResponseEntity<CreateToolRsp> createToolV1(String projectId, String workspaceId, CreateToolReq body) {
        return ResponseModel.success(toolManagementService.createToolV1(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteToolCredential(String projectId, String toolId, String workspaceId) {
        return ResponseModel.success(toolManagementService.deleteToolCredential(projectId, toolId, workspaceId));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteToolV1(String projectId, String toolId, String workspaceId) {
        return ResponseModel.success(toolManagementService.deleteToolV1(projectId, toolId, workspaceId));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteToolVersion(String projectId, String versionId, String toolId,
        String workspaceId) {
        return ResponseModel.success(
            toolManagementService.deleteToolVersion(projectId, versionId, toolId, workspaceId));
    }

    @Override
    public ResponseEntity<Tool> getToolVersion(String projectId, String versionId, String toolId,
        GetToolVersionQo getToolVersionQo) {
        return ResponseModel.success(
            toolManagementService.getToolVersion(projectId, versionId, toolId, getToolVersionQo));
    }

    @Override
    public ResponseEntity<VersionListRsp> listToolVersions(String projectId, String toolId, String workspaceId) {
        return ResponseModel.success(toolManagementService.listToolVersions(projectId, toolId, workspaceId));
    }

    @Override
    public ResponseEntity<ToolListRsp> listToolsV1(String projectId, ListToolsV1Qo listToolsV1Qo) {
        return ResponseModel.success(toolManagementService.listToolsV1(projectId, listToolsV1Qo));
    }

    @Override
    public ResponseEntity<ModifyToolRsp> modifyToolV1(String projectId, String workspaceId, String toolId,
        ModifyToolReq body) {
        return ResponseModel.success(toolManagementService.modifyToolV1(projectId, workspaceId, toolId, body));
    }

    @Override
    public ResponseEntity<VersionInfo> releaseToolVersion(String projectId, String toolId, String workspaceId,
        CreateVersionReq body) {
        return ResponseModel.success(toolManagementService.releaseToolVersion(projectId, toolId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Tool> retrieveToolV1(String projectId, String toolId, String workspaceId) {
        return ResponseModel.success(toolManagementService.retrieveToolV1(projectId, toolId, workspaceId));
    }
}