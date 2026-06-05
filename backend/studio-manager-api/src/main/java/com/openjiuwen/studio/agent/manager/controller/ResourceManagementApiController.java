/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ResourceUsageDetails;
import com.openjiuwen.studio.agent.manager.service.IResourceManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ResourceManagement controller
 */
@RestController

public class ResourceManagementApiController implements ResourceManagementApi {
    private static final Logger log = LoggerFactory.getLogger(ResourceManagementApiController.class);

    @Autowired
    private IResourceManagementService resourceManagementService;

    @Override
    public ResponseEntity<ResourceUsageDetails> resourceUsageDetails(String projectId, String resourceType) {
        return ResponseModel.success(resourceManagementService.resourceUsageDetails(projectId, resourceType));
    }
}