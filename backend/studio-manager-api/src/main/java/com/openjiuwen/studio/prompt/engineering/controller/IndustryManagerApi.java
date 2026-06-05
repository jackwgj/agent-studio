/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.prompt.engineering.dto.IndustryVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * IndustryManagerApi interface
 */

@Api(value = "IndustryManager", description = "the IndustryManager API")
@Validated
public interface IndustryManagerApi {

    @ApiOperation(value = "", nickname = "listIndustry", notes = "", response = IndustryVo.class,
        responseContainer = "List", tags = {"IndustryManager"})
    @ApiResponses(
        value = {@ApiResponse(code = 200, message = "", response = IndustryVo.class, responseContainer = "List")})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt/industry/list", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<IndustryVo>> listIndustry(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @ApiParam(value = "工作空间ID") @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @ApiParam(value = "组件类型") @RequestParam(value = "library_type", required = false) String libraryType);
}
