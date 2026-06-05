/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.ResourceClearResp;
import com.openjiuwen.studio.agent.runtime.service.IResourcesCleanerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * ResourcesCleaner controller
 */
@RestController

public class ResourcesCleanerApiController implements ResourcesCleanerApi {
    private static final Logger log = LoggerFactory.getLogger(ResourcesCleanerApiController.class);

    @Autowired
    private IResourcesCleanerService resourcesCleanerService;

    @Override
    public ResponseEntity<ResourceClearResp> clearResource(String projectId, String resourceId, String type) {
        return ResponseModel.success(resourcesCleanerService.clearResource(projectId, resourceId, type));
    }
}