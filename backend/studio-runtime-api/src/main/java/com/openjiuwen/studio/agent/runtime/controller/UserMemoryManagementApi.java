/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.dto.ClearLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesQo;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;

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
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "UserMemoryManagement", description = "the UserMemoryManagement API")
@Validated

/**
 * UserMemoryManagementApi interface
 */
public interface UserMemoryManagementApi {
    @ApiOperation(value = "清空某个记忆库中的用户记忆内容", nickname = "clearLongTermMemories",
        notes = "清空某个记忆库中的用户记忆内容", response = ClearLongTermMemoriesResponseBody.class,
        tags = {"UserMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "清空结果", response = ClearLongTermMemoriesResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-runtime/memories/long-terms", produces = {"application/json"},
        method = RequestMethod.DELETE)
    ResponseEntity<ClearLongTermMemoriesResponseBody> clearLongTermMemories(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @Size(min = 1, max = 64) @ApiParam(value = "记忆库id", required = true)
        @RequestParam(value = "memory_repo_id", required = true) String memoryRepoId);

    @ApiOperation(value = "删除记忆", nickname = "deleteLongTermMemories", notes = "删除指定记忆内容",
        response = DeleteLongTermMemoriesResponseBody.class, tags = {"UserMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "删除用户记忆内容的值的响应体",
            response = DeleteLongTermMemoriesResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-runtime/memories/long-terms/batch-delete",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<DeleteLongTermMemoriesResponseBody> deleteLongTermMemories(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @Size(min = 1, max = 64) @ApiParam(value = "记忆库id", required = true)
        @RequestParam(value = "memory_repo_id", required = true) String memoryRepoId,
        @NotNull @ApiParam(value = "删除用户记忆内容的请求体", required = true) @Valid @RequestBody
        DeleteLongTermMemoriesRequestBody body);

    @ApiOperation(value = "查看某个应用内记忆内容列表", nickname = "listLongTermMemories",
        notes = "查看当前登录用户在某个记忆库内的记忆内容列表", response = ListLongTermMemoriesResponseBody.class,
        tags = {"UserMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "查看记忆内容列表的响应体",
            response = ListLongTermMemoriesResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-runtime/memories/long-terms", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<ListLongTermMemoriesResponseBody> listLongTermMemories(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @ApiParam(value = "ListLongTermMemoriesQo: converted from multi query params") @Valid
        ListLongTermMemoriesQo listLongTermMemoriesQo);

    @ApiOperation(value = "修改记忆内容", nickname = "updateLongTermMemories", notes = "更新长期记忆的内容",
        response = UpdateLongTermMemoriesResponseBody.class, tags = {"UserMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "修改用户记忆内容的结果",
            response = UpdateLongTermMemoriesResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agent-runtime/memories/long-terms", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.PUT)
    ResponseEntity<UpdateLongTermMemoriesResponseBody> updateLongTermMemories(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @NotNull @Size(min = 1, max = 64) @ApiParam(value = "记忆库id", required = true)
        @RequestParam(value = "memory_repo_id", required = true) String memoryRepoId,
        @NotNull @ApiParam(value = "修改用户画像记忆值的请求体", required = true) @Valid @RequestBody
        UpdateLongTermMemoriesRequestBody body);

}
