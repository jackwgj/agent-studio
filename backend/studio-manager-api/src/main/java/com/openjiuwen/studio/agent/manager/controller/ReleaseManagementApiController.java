/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ReleaseAppInfo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveReleaseAppInfoQo;
import com.openjiuwen.studio.agent.manager.service.IReleaseManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReleaseManagement controller
 */
@RestController

public class ReleaseManagementApiController implements ReleaseManagementApi {
    private static final Logger log = LoggerFactory.getLogger(ReleaseManagementApiController.class);

    @Autowired
    private IReleaseManagementService releaseManagementService;

    @Override
    public ResponseEntity<ReleaseAppInfo> retrieveReleaseAppInfo(String projectId, String releaseId,
        RetrieveReleaseAppInfoQo retrieveReleaseAppInfoQo) {
        return ResponseModel.success(
            releaseManagementService.retrieveReleaseAppInfo(projectId, releaseId, retrieveReleaseAppInfoQo));
    }
}