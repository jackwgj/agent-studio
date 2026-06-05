/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "Health", description = "the Health API")
@Validated

/**
 * HealthApi interface
 */ public interface HealthApi {
    @ApiOperation(value = "健康检查", nickname = "healthCheck", notes = "健康检查。", response = String.class,
        tags = {"Health"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "应用状态正常。", response = String.class),
        @ApiResponse(code = 400, message = "请求错误。", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/health", produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<String> healthCheck();

    @ApiOperation(value = "健康检查", nickname = "managerHealthCheck", notes = "健康检查。", response = String.class,
        tags = {"Health"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "应用状态正常。", response = String.class),
        @ApiResponse(code = 400, message = "请求错误。", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/agent-manager/health", produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<String> managerHealthCheck();

}
