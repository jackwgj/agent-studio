/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.prompt.engineering.dto.ListTagsQo;
import com.openjiuwen.studio.prompt.engineering.dto.TagListVo;

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

/**
 * TagManagerApi interface
 */

@Api(value = "TagManager", description = "the TagManager API")
@Validated
public interface TagManagerApi {

    @ApiOperation(value = "", nickname = "listTags", notes = "", response = TagListVo.class, tags = {"TagManager"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = TagListVo.class)})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt/tag/list", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<TagListVo> listTags(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @ApiParam(value = "ListTagsQo: converted from multi query params") @Valid ListTagsQo listTagsQo);
}
