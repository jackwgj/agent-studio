/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.dto.ControllerExecutionDetail;
import com.openjiuwen.studio.agent.common.dto.run.GetControllerExecutionDetailQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversationsQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversionsResp;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsQo;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsResp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(value = "ControllerConversationMgmt", description = "the ControllerConversationMgmt API")
@Validated

/**
 * ControllerConversationMgmtApi interface
 */
public interface ControllerConversationMgmtApi {
    @ApiOperation(value = "", nickname = "getControllerExecutionDetail", notes = "查询多智能体会话的指定对话详情",
        response = ControllerExecutionDetail.class, tags = {"ControllerConversationMgmt"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Controller会话的的对话详情", response = ControllerExecutionDetail.class)
    })
    @RequestMapping(value = "/v1/{project_id}/controller/{agent_id}/executions/{execution_id}",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<ControllerExecutionDetail> getControllerExecutionDetail(
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId,
        @Size(max = 64) @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("execution_id") String executionId,
        @ApiParam(value = "GetControllerExecutionDetailQo: converted from multi query params") @Valid
        GetControllerExecutionDetailQo getControllerExecutionDetailQo);

    @ApiOperation(value = "", nickname = "listControllerConversations", notes = "查询用户的Controller对话列表",
        response = ListControllerConversionsResp.class, tags = {"ControllerConversationMgmt"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "", response = ListControllerConversionsResp.class),
        @ApiResponse(code = 400, message = "", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/controller/{agent_id}/conversations", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<ListControllerConversionsResp> listControllerConversations(
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId,
        @ApiParam(value = "ListControllerConversationsQo: converted from multi query params") @Valid
        ListControllerConversationsQo listControllerConversationsQo);

    @ApiOperation(value = "", nickname = "listControllerExecutions", notes = "查询用户的Controller对话列表",
        response = ListControllerExecutionsResp.class, tags = {"ControllerConversationMgmt"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Controller对话query列表", response = ListControllerExecutionsResp.class),
        @ApiResponse(code = 400, message = "", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/controller/{agent_id}/conversations/{conversation_id}/executions",
        produces = {"application/json"}, method = RequestMethod.GET)
    ResponseEntity<ListControllerExecutionsResp> listControllerExecutions(
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @ApiParam(value = "ListControllerExecutionsQo: converted from multi query params") @Valid
        ListControllerExecutionsQo listControllerExecutionsQo);

}
