/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.dto.knowledge.ListUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.BatchDeleteUserVariableMemoryRequestBody;
import com.openjiuwen.studio.agent.common.dto.BatchDeleteUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.common.dto.run.ResetUserVariableMemoryResponseBody;

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

@Api(value = "AgentMemoryManagement", description = "the AgentMemoryManagement API")
@Validated

/**
 * AgentMemoryManagementApi interface
 */ public interface AgentMemoryManagementApi {
    @ApiOperation(value = "批量删除用户的变量记忆", nickname = "batchDeleteUserVariableMemory",
        notes = "批量删除用户的变量记忆（用于Agent运行时用户手动删除自己的变量记忆）",
        response = BatchDeleteUserVariableMemoryResponseBody.class, tags = {"AgentMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "批量删除变量记忆的响应体",
            response = BatchDeleteUserVariableMemoryResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v2/{project_id}/agent-runtime/agents/{agent_id}/memories/variables/batch-delete",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<BatchDeleteUserVariableMemoryResponseBody> batchDeleteUserVariableMemory(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "AgentID或Agent发布之后的短编码", required = true,
            schema = @Schema()) @PathVariable("agent_id") String agentId,
        @Size(max = 64) @ApiParam(value = "空间ID") @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @NotNull @ApiParam(value = "批量删除变量记忆的请求体", required = true) @Valid @RequestBody
        BatchDeleteUserVariableMemoryRequestBody body);

    @ApiOperation(value = "查询应用中的用户的变量记忆列表", nickname = "listUserVariableMemory",
        notes = "查询应用中的用户的变量记忆列表（用于Agent运行时查询变量列表）",
        response = ListUserVariableMemoryResponseBody.class, tags = {"AgentMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "变量记忆列表", response = ListUserVariableMemoryResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v2/{project_id}/agent-runtime/agents/{agent_id}/memories/variables",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<ListUserVariableMemoryResponseBody> listUserVariableMemory(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "AgentID或Agent发布之后的短编码", required = true,
            schema = @Schema()) @PathVariable("agent_id") String agentId,
        @Size(max = 100) @ApiParam(value = "空间ID") @RequestParam(value = "workspace_id", required = false)
        String workspaceId);

    @ApiOperation(value = "重置用户的变量记忆", nickname = "resetUserVariableMemory",
        notes = "重置用户的变量记忆（用于Agent运行时用户手动重置自己的变量记忆），重置后，用户的所有变量值将重置为默认值",
        response = ResetUserVariableMemoryResponseBody.class, tags = {"AgentMemoryManagement"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "批量删除变量记忆的响应体",
            response = ResetUserVariableMemoryResponseBody.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class),
        @ApiResponse(code = 401, message = "Unauthorized 鉴权失败", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden 没有操作权限", response = ErrorRsp.class),
        @ApiResponse(code = 404, message = "Not Found 找不到资源", response = ErrorRsp.class),
        @ApiResponse(code = 500, message = "Internal Server Error 服务内部错误", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v2/{project_id}/agent-runtime/agents/{agent_id}/memories/variables/reset",
        produces = {"application/json"}, method = RequestMethod.POST)
    ResponseEntity<ResetUserVariableMemoryResponseBody> resetUserVariableMemory(@Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Size(min = 1, max = 64)
        @Parameter(in = ParameterIn.PATH, description = "AgentID或Agent发布之后的短编码", required = true,
            schema = @Schema()) @PathVariable("agent_id") String agentId,
        @Size(max = 64) @ApiParam(value = "空间ID") @RequestParam(value = "workspace_id", required = false)
        String workspaceId);

}
