/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("")
public class HealthController {
    @Value("${runtime.health.content:success}")
    private String checkContent;

    @RequestMapping(value = "/v1/health", produces = {"application/json"}, method = RequestMethod.GET)
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok(checkContent);
    }
}
