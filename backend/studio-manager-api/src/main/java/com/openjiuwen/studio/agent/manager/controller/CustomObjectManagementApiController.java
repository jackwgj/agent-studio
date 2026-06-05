/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ListCustomObjectQo;
import com.openjiuwen.studio.agent.manager.dto.ObjectBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ObjectInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ObjectListRsp;
import com.openjiuwen.studio.agent.manager.dto.ObjectRsp;
import com.openjiuwen.studio.agent.manager.service.ICustomObjectManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * CustomObjectManagement controller
 */
@RestController

public class CustomObjectManagementApiController implements CustomObjectManagementApi {
    private static final Logger log = LoggerFactory.getLogger(CustomObjectManagementApiController.class);

    @Autowired
    private ICustomObjectManagementService customObjectManagementService;

    @Override
    public ResponseEntity<ObjectBriefRsp> createCustomObject(String projectId, String workspaceId, ObjectInfoReq body) {
        return ResponseModel.success(customObjectManagementService.createCustomObject(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ObjectBriefRsp> deleteCustomObject(String projectId, String id, String workspaceId) {
        return ResponseModel.success(customObjectManagementService.deleteCustomObject(projectId, id, workspaceId));
    }

    @Override
    public ResponseEntity<ObjectListRsp> listCustomObject(String projectId, ListCustomObjectQo listCustomObjectQo) {
        return ResponseModel.success(customObjectManagementService.listCustomObject(projectId, listCustomObjectQo));
    }

    @Override
    public ResponseEntity<ObjectBriefRsp> modifyCustomObject(String projectId, String id, String workspaceId,
        ObjectInfoReq body) {
        return ResponseModel.success(
            customObjectManagementService.modifyCustomObject(projectId, id, workspaceId, body));
    }

    @Override
    public ResponseEntity<ObjectRsp> retrieveCustomObject(String projectId, String id, String workspaceId) {
        return ResponseModel.success(customObjectManagementService.retrieveCustomObject(projectId, id, workspaceId));
    }
}