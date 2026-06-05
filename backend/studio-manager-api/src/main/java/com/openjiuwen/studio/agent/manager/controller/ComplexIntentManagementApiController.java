/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchListRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentExportParams;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentImportRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentRsp;
import com.openjiuwen.studio.agent.manager.dto.ListComplexIntentBranchQo;
import com.openjiuwen.studio.agent.manager.dto.ListComplexIntentQo;
import com.openjiuwen.studio.agent.manager.service.IComplexIntentManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ComplexIntentManagement controller
 */
@RestController

public class ComplexIntentManagementApiController implements ComplexIntentManagementApi {
    private static final Logger log = LoggerFactory.getLogger(ComplexIntentManagementApiController.class);

    @Autowired
    private IComplexIntentManagementService complexIntentManagementService;

    @Override
    public ResponseEntity<ComplexIntentBriefRsp> createComplexIntent(String projectId, String workspaceId,
        ComplexIntentInfoReq body) {
        return ResponseModel.success(complexIntentManagementService.createComplexIntent(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ComplexIntentBranchBriefRsp> createComplexIntentBranch(String projectId, String intentId,
        String workspaceId, ComplexIntentBranchInfoReq body) {
        return ResponseModel.success(
            complexIntentManagementService.createComplexIntentBranch(projectId, intentId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ComplexIntentBriefRsp> deleteComplexIntent(String projectId, String intentId,
        String workspaceId) {
        return ResponseModel.success(
            complexIntentManagementService.deleteComplexIntent(projectId, intentId, workspaceId));
    }

    @Override
    public ResponseEntity<ComplexIntentBranchBriefRsp> deleteComplexIntentBranch(String projectId, String intentId,
        String branchId, String workspaceId) {
        return ResponseModel.success(
            complexIntentManagementService.deleteComplexIntentBranch(projectId, intentId, branchId, workspaceId));
    }

    @Override
    public ResponseEntity<Resource> exportIntent(String projectId, String workspaceId, ComplexIntentExportParams body) {
        return ResponseModel.successDownload(complexIntentManagementService.exportIntent(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Resource> exportIntentTemplate(String projectId, String workspaceId) {
        return ResponseModel.successDownload(
            complexIntentManagementService.exportIntentTemplate(projectId, workspaceId));
    }

    @Override
    public ResponseEntity<ComplexIntentImportRsp> importIntent(String workspaceId, String projectId, MultipartFile file,
        String intentName) {
        return ResponseModel.success(
            complexIntentManagementService.importIntent(workspaceId, projectId, file, intentName));
    }

    @Override
    public ResponseEntity<ComplexIntentImportRsp> importIntentBranch(String workspaceId, String projectId,
        String intentId, MultipartFile file) {
        return ResponseModel.success(
            complexIntentManagementService.importIntentBranch(workspaceId, projectId, intentId, file));
    }

    @Override
    public ResponseEntity<ComplexIntentListRsp> listComplexIntent(String projectId,
        ListComplexIntentQo listComplexIntentQo) {
        return ResponseModel.success(complexIntentManagementService.listComplexIntent(projectId, listComplexIntentQo));
    }

    @Override
    public ResponseEntity<ComplexIntentBranchListRsp> listComplexIntentBranch(String projectId, String intentId,
        ListComplexIntentBranchQo listComplexIntentBranchQo) {
        return ResponseModel.success(
            complexIntentManagementService.listComplexIntentBranch(projectId, intentId, listComplexIntentBranchQo));
    }

    @Override
    public ResponseEntity<ComplexIntentBriefRsp> modifyComplexIntent(String projectId, String intentId,
        String workspaceId, ComplexIntentInfoReq body) {
        return ResponseModel.success(
            complexIntentManagementService.modifyComplexIntent(projectId, intentId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ComplexIntentBranchBriefRsp> modifyComplexIntentBranch(String projectId, String intentId,
        String branchId, String workspaceId, ComplexIntentBranchInfoReq body) {
        return ResponseModel.success(
            complexIntentManagementService.modifyComplexIntentBranch(projectId, intentId, branchId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ComplexIntentRsp> retrieveComplexIntent(String projectId, String intentId,
        String workspaceId) {
        return ResponseModel.success(
            complexIntentManagementService.retrieveComplexIntent(projectId, intentId, workspaceId));
    }
}