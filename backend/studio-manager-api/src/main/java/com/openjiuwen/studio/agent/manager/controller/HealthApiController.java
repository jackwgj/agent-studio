/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.service.IHealthService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health controller
 */
@RestController

public class HealthApiController implements HealthApi {
    private static final Logger log = LoggerFactory.getLogger(HealthApiController.class);

    @Autowired
    private IHealthService healthService;

    @Override
    public ResponseEntity<String> healthCheck() {
        return ResponseModel.success(healthService.healthCheck());
    }

    @Override
    public ResponseEntity<String> managerHealthCheck() {
        return ResponseModel.success(healthService.managerHealthCheck());
    }
}