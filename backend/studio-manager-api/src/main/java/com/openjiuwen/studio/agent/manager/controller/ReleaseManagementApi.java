/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.manager.dto.ReleaseAppInfo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveReleaseAppInfoQo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "ReleaseManagement", description = "the ReleaseManagement API")
@Validated

/**
 * ReleaseManagementApi interface
 */ public interface ReleaseManagementApi {
    @ApiOperation(value = "获取一个发布应用信息", nickname = "retrieveReleaseAppInfo", notes = "获取一个发布应用信息。",
        response = ReleaseAppInfo.class, tags = {"ReleaseManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "发布应用详情信息。", response = ReleaseAppInfo.class),
        @ApiResponse(code = 400, message = "请求错误。", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-manager/releases/{release_id}", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<ReleaseAppInfo> retrieveReleaseAppInfo(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("release_id") String releaseId,
        @ApiParam(value = "RetrieveReleaseAppInfoQo: converted from multi query params") @Valid
        RetrieveReleaseAppInfoQo retrieveReleaseAppInfoQo);

}
