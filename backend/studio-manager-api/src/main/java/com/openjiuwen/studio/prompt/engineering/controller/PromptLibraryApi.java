/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateListVo;
import com.openjiuwen.studio.prompt.engineering.dto.QueryTemplateCondition;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PromptLibraryApi interface
 */

@Api(value = "PromptLibrary", description = "the PromptLibrary API")
@Validated
public interface PromptLibraryApi {

    @ApiOperation(value = "", nickname = "deletePromptTemplate", notes = "删除模板", response = String.class,
        tags = {"PromptLibrary"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = String.class)})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt-engineering/template/delete/{id}",
        produces = {"application/json"}, method = RequestMethod.DELETE)
    ResponseEntity<String> deletePromptTemplate(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema()) @PathVariable("id")
        String id,
        @NotNull @ApiParam(value = "工作空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId);

    @ApiOperation(value = "", nickname = "downloadPromptSampleTemplate", notes = "导出模板样例",
        response = String.class, tags = {"PromptLibrary"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "success", response = String.class)})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt-engineering/template/sample/download",
        produces = {"application/json", "application/excel"}, method = RequestMethod.POST)
    ResponseEntity<String> downloadPromptSampleTemplate(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "工作空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId);

    @ApiOperation(value = "", nickname = "downloadPromptTemplateV2", notes = "导出模板", response = String.class,
        tags = {"PromptLibrary"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "success", response = String.class)})
    @RequestMapping(value = "/v2/{project_id}/agent-builder/prompt-engineering/template/download",
        produces = {"application/json", "application/excel"}, consumes = {"application/json"},
        method = RequestMethod.POST)
    ResponseEntity<String> downloadPromptTemplateV2(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "工作空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @ApiParam(value = "Template 模板 ID") @Valid @RequestBody(required = false) List<String> body);

    @ApiOperation(value = "", nickname = "importPromptTemplateV2", notes = "导入模板", response = String.class,
        responseContainer = "List", tags = {"PromptLibrary"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = String.class, responseContainer = "List")})
    @RequestMapping(value = "/v2/{project_id}/agent-builder/prompt-engineering/template/import",
        produces = {"application/json"}, consumes = {"multipart/form-data"}, method = RequestMethod.POST)
    ResponseEntity<List<String>> importPromptTemplateV2(
        @NotNull @ApiParam(value = "工作空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId, @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Parameter(description = "file detail") @Valid @RequestPart(value = "file", required = true)
        MultipartFile file);

    @ApiOperation(value = "", nickname = "listPromptTemplates", notes = "查询提示词",
        response = PePromptTemplateListVo.class, tags = {"PromptLibrary"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = PePromptTemplateListVo.class)})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt-engineering/template/list",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<PePromptTemplateListVo> listPromptTemplates(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "工作空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @ApiParam(value = "分类") @RequestParam(value = "category", required = false) String category,
        @ApiParam(value = "") @Valid @RequestBody(required = false) QueryTemplateCondition body);

    @ApiOperation(value = "", nickname = "savePromptTemplate", notes = "保存模板", response = String.class,
        tags = {"PromptLibrary"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = String.class)})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt-engineering/template/save",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<String> savePromptTemplate(
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "工作空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "创建模板请求体", required = true) @Valid @RequestBody PePromptTemplateDto body);
}
