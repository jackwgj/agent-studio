/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.NotifyReq;
import com.openjiuwen.studio.agent.manager.dto.NotifyResp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "RosCleanResource", description = "the RosCleanResource API")
@Validated

/**
 * RosCleanResourceApi interface
 */ public interface RosCleanResourceApi {
    @ApiOperation(value = "数据清理", nickname = "cleanNotify", notes = "数据清理通知接口", response = NotifyResp.class,
        tags = {"RosCleanResource"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "清理结果", response = NotifyResp.class)
    })
    @RequestMapping(value = "/v1/agent-manager/{module_name}/cleannotify", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<NotifyResp> cleanNotify(@Pattern(regexp = "^[a-zA-Z0-9_-]{1,40}$")
        @Parameter(in = ParameterIn.PATH, description = "模块名", required = true, schema = @Schema())
        @PathVariable("module_name") String moduleName,
        @NotNull @ApiParam(value = "", required = true) @Valid @RequestBody NotifyReq body);

}
