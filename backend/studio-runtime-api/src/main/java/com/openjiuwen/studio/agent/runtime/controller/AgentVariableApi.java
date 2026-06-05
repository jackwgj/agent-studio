/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.dto.AgentVariableRsp;
import com.openjiuwen.studio.agent.runtime.dto.GetAgentVariableQo;
import com.openjiuwen.studio.agent.runtime.dto.UpdateVariableReq;
import com.openjiuwen.studio.agent.runtime.dto.VariableInfo;

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

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "AgentVariable", description = "the AgentVariable API")
@Validated

/**
 * AgentVariableApi interface
 */
public interface AgentVariableApi {
    @ApiOperation(value = "", nickname = "getAgentVariable", notes = "获取资源全局变量",
        response = AgentVariableRsp.class, tags = {"AgentVariable"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "success", response = AgentVariableRsp.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agents/{agent_id}/conversations/{conversation_id}/variables",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<AgentVariableRsp> getAgentVariable(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Parameter(in = ParameterIn.PATH, description = "资源ID", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId,
        @Parameter(in = ParameterIn.PATH, description = "会话ID", required = true, schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @ApiParam(value = "GetAgentVariableQo: converted from multi query params") @Valid
        GetAgentVariableQo getAgentVariableQo);

    @ApiOperation(value = "", nickname = "updateAgentVariable", notes = "更新会话变量", response = VariableInfo.class,
        tags = {"AgentVariable"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "success", response = VariableInfo.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agents/{agent_id}/conversations/{conversation_id}/variables/{var_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.PUT)
    ResponseEntity<VariableInfo> updateAgentVariable(
        @Parameter(in = ParameterIn.PATH, description = "项目ID", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId,
        @Parameter(in = ParameterIn.PATH, description = "资源ID", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId,
        @Parameter(in = ParameterIn.PATH, description = "会话ID", required = true, schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @Parameter(in = ParameterIn.PATH, description = "变量名称", required = true, schema = @Schema())
        @PathVariable("var_id") String varId,
        @NotNull @ApiParam(value = "接口请求体", required = true) @Valid @RequestBody UpdateVariableReq body);

}
