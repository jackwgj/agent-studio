/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ExportMessagesParams;
import com.openjiuwen.studio.agent.manager.dto.ListStructuredMessagesQo;
import com.openjiuwen.studio.agent.manager.dto.StructMessage;
import com.openjiuwen.studio.agent.manager.dto.StructMessageListRsp;
import com.openjiuwen.studio.agent.manager.dto.StructuredInfoRequest;
import com.openjiuwen.studio.agent.manager.dto.StructuredInfoRequestDelete;
import com.openjiuwen.studio.agent.manager.dto.StructuredMessagesUploadResult;
import com.openjiuwen.studio.agent.manager.service.IMessageManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * MessageManagement controller
 */
@RestController

public class MessageManagementApiController implements MessageManagementApi {
    private static final Logger log = LoggerFactory.getLogger(MessageManagementApiController.class);

    @Autowired
    private IMessageManagementService messageManagementService;

    @Override
    public ResponseEntity<StructMessage> createStructuredMessages(String projectId, String workspaceId,
        StructuredInfoRequest body) {
        return ResponseModel.success(messageManagementService.createStructuredMessages(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<DatasourceBatchDeleteRsp> deleteStructuredMessages(String projectId, String workspaceId,
        List<StructuredInfoRequestDelete> body) {
        return ResponseModel.success(messageManagementService.deleteStructuredMessages(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Resource> exportStructuredMessages(String projectId, String workspaceId,
        ExportMessagesParams body) {
        return ResponseModel.successDownload(
            messageManagementService.exportStructuredMessages(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Resource> exportStructuredTemplate(String projectId, String workspaceId) {
        return ResponseModel.successDownload(messageManagementService.exportStructuredTemplate(projectId, workspaceId));
    }

    @Override
    public ResponseEntity<StructMessageListRsp> listStructuredMessages(String projectId,
        ListStructuredMessagesQo listStructuredMessagesQo) {
        return ResponseModel.success(
            messageManagementService.listStructuredMessages(projectId, listStructuredMessagesQo));
    }

    @Override
    public ResponseEntity<StructMessage> updateStructuredMessages(String projectId, String workspaceId,
        StructuredInfoRequest body) {
        return ResponseModel.success(messageManagementService.updateStructuredMessages(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<StructuredMessagesUploadResult> uploadStructuredMessages(String workspaceId, String projectId,
        MultipartFile file) {
        return ResponseModel.success(messageManagementService.uploadStructuredMessages(workspaceId, projectId, file));
    }
}