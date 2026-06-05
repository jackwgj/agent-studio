/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.prompt.engineering.dto.BaseInfo;
import com.openjiuwen.studio.prompt.engineering.dto.ListEvalDataSetQo;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBaseResp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptErrorInfo;
import com.openjiuwen.studio.prompt.engineering.dto.PromptEvalDataItem;

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

import org.springframework.core.io.Resource;
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
 * PromptEngineerDataSetApi interface
 */

@Api(value = "PromptEngineerDataSet", description = "the PromptEngineerDataSet API")
@Validated
public interface PromptEngineerDataSetApi {

    @ApiOperation(value = "向优化任务批量添加数据用例", nickname = "addBatchEvalDataSetItem",
        notes = "Add batch data item to an existing dataset", response = BaseInfo.class,
        tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "添加成功", response = BaseInfo.class),
        @ApiResponse(code = 400, message = "操作失败", response = PromptErrorInfo.class),
        @ApiResponse(code = 404, message = "数据集不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/data/batchItems", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<BaseInfo> addBatchEvalDataSetItem(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @NotNull @ApiParam(value = "空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "", required = true) @Valid @RequestBody List<PromptEvalDataItem> body);

    @ApiOperation(value = "向优化任务添加单条数据用例", nickname = "addSingleEvalDataSetItem",
        notes = "Add a single data item to an existing dataset", response = BaseInfo.class,
        tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "添加成功", response = BaseInfo.class),
        @ApiResponse(code = 400, message = "操作失败", response = PromptErrorInfo.class),
        @ApiResponse(code = 404, message = "数据集不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/data/items", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<BaseInfo> addSingleEvalDataSetItem(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @NotNull @ApiParam(value = "空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "", required = true) @Valid @RequestBody PromptEvalDataItem body);

    @ApiOperation(value = "删除优化任务中的单条用例", nickname = "deleteEvalDataSetItem",
        notes = "Delete a specific data item from a dataset", response = BaseInfo.class,
        tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "删除成功", response = BaseInfo.class),
        @ApiResponse(code = 404, message = "数据集或数据用例不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/data/items/{item_id}",
        produces = {"application/json"}, method = RequestMethod.DELETE)
    ResponseEntity<BaseInfo> deleteEvalDataSetItem(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @Parameter(in = ParameterIn.PATH, description = "数据用例ID", required = true, schema = @Schema())
        @PathVariable("item_id") String itemId,
        @NotNull @ApiParam(value = "空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId);

    @ApiOperation(value = "下载数据用例模板", nickname = "downloadEvalDataSetTemplate",
        notes = "下载一个包含 data.json 和图片文件夹的压缩包模板，用于数据导入", response = Resource.class,
        tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "下载成功，返回压缩包", response = Resource.class),
        @ApiResponse(code = 400, message = "参数缺失或格式错误", response = PromptErrorInfo.class),
        @ApiResponse(code = 404, message = "任务或项目不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器内部错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/data/download", produces = {"application/zip"},
        method = RequestMethod.GET)
    ResponseEntity<Resource> downloadEvalDataSetTemplate(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId, @ApiParam(value = "提示词优化任务类型", allowableValues = "text, multi")
        @RequestParam(value = "ptType", required = false) String ptType);

    @ApiOperation(value = "通过文件导入批量添加数据用例", nickname = "importEvalDataSetItems",
        notes = "Import multiple data items to a dataset via file upload (supports jsonl formats)",
        response = BaseInfo.class, tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "添加成功", response = BaseInfo.class),
        @ApiResponse(code = 400, message = "操作失败", response = PromptErrorInfo.class),
        @ApiResponse(code = 404, message = "数据集不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/data/import", produces = {"application/json"},
        consumes = {"multipart/form-data"}, method = RequestMethod.POST)
    ResponseEntity<BaseInfo> importEvalDataSetItems(
        @NotNull @ApiParam(value = "空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @Parameter(description = "file detail") @Valid @RequestPart(value = "file", required = true)
        MultipartFile file);

    @ApiOperation(value = "查询优化任务数据列表", nickname = "listEvalDataSet",
        notes = "Get list of a specific dataset including its items", response = PromptBaseResp.class,
        tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "操作成功", response = PromptBaseResp.class),
        @ApiResponse(code = 404, message = "数据集不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/data/list", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<PromptBaseResp> listEvalDataSet(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @ApiParam(value = "ListEvalDataSetQo: converted from multi query params") @Valid
        ListEvalDataSetQo listEvalDataSetQo);

    @ApiOperation(value = "更新优化任务中的单条用例", nickname = "updateEvalDataSetItem",
        notes = "Update a specific data item in a dataset", response = PromptEvalDataItem.class,
        tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "更新成功", response = PromptEvalDataItem.class),
        @ApiResponse(code = 400, message = "操作失败", response = PromptErrorInfo.class),
        @ApiResponse(code = 404, message = "数据集或数据用例不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "服务器错误", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/data/items/{item_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.PUT)
    ResponseEntity<PromptEvalDataItem> updateEvalDataSetItem(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @Parameter(in = ParameterIn.PATH, description = "数据用例ID", required = true, schema = @Schema())
        @PathVariable("item_id") String itemId,
        @NotNull @ApiParam(value = "空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "", required = true) @Valid @RequestBody PromptEvalDataItem body);

    @ApiOperation(value = "上传图片，获得临时url", nickname = "uploadPicture", notes = "Import picture to get temp url",
        response = PromptBaseResp.class, tags = {"Prompt Engineer DataSet"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "操作成功", response = PromptBaseResp.class),
        @ApiResponse(code = 400, message = "操作失败,返回错误信息", response = PromptErrorInfo.class),
        @ApiResponse(code = 404, message = "任务不存在", response = PromptErrorInfo.class),
        @ApiResponse(code = 500, message = "操作失败,返回错误信息", response = PromptErrorInfo.class)
    })
    @RequestMapping(value = "/v2/{project_id}/prompt/tasks/{task_id}/upload/picture", produces = {"application/json"},
        consumes = {"multipart/form-data"}, method = RequestMethod.POST)
    ResponseEntity<PromptBaseResp> uploadPicture(
        @NotNull @ApiParam(value = "空间id", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        @Parameter(in = ParameterIn.PATH, description = "优化任务ID", required = true, schema = @Schema())
        @PathVariable("task_id") String taskId,
        @Parameter(description = "file detail") @Valid @RequestPart(value = "file", required = true)
        MultipartFile file);
}
