/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.common.dto.knowledge.FileUploadRsp;
import com.openjiuwen.studio.agent.manager.dto.TagInfo;
import com.openjiuwen.studio.agent.manager.service.ICommonManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * CommonManagement controller
 */
@RestController

public class CommonManagementApiController implements CommonManagementApi {
    private static final Logger log = LoggerFactory.getLogger(CommonManagementApiController.class);

    @Autowired
    private ICommonManagementService commonManagementService;

    @Override
    public ResponseEntity<List<TagInfo>> listTags(String projectId, String workspaceId) {
        return ResponseModel.success(commonManagementService.listTags(projectId, workspaceId));
    }

    @Override
    public ResponseEntity<Object> systemSettings(String projectId, String service) {
        return ResponseModel.success(commonManagementService.systemSettings(projectId, service));
    }

    @Override
    public ResponseEntity<FileUploadRsp> uploadAvatar(String projectId, MultipartFile file) {
        return ResponseModel.success(commonManagementService.uploadAvatar(projectId, file));
    }
}