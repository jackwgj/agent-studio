/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.dto.AgentRunReq;
import com.openjiuwen.studio.agent.runtime.dto.AgentRunRsp;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunReq;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunRsp;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能描述
 *
 */
@Validated
@RestController
@Slf4j
public class ConversationsInnerController {

    @Resource
    private AgentRuntimeController agentRuntimeController;

    @Resource
    private WorkflowRuntimeController workflowRuntimeController;

    @ApiOperation(value = "run agent", nickname = "runAgentWithConversation", notes = "运行知识型Agent",
        response = AgentRunRsp.class, tags = {"AgentRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AgentRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/runtime/inner/v1/{project_id}/agents/{agent_id}/conversations/{conversation_id}",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runAgentWithConversation(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "Agent类型", schema = @Schema())
        @RequestParam(value = "agent_type", required = false) String agentType,
        @Parameter(in = ParameterIn.PATH, description = "agent id", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @RequestParam(value = "version", required = false) String version,
        @RequestHeader(value = "X-Invoke-Mode", required = false) String invokeMode,
        @RequestParam(value = "type", required = false) String type,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @RequestHeader(value = "X-Invoke-Source", required = false) String invokeSource,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody AgentRunReq body) {
        return agentRuntimeController.runWithConversation(projectId, workspaceId, agentId, conversationId, version,
            invokeMode, type, stream, invokeSource, projectId, workspaceId, body, true,
            RequestContextUtils.getRequestUserDomainId(), agentType, executionId);
    }

    @ApiOperation(value = "run workflow applications", nickname = "runWorkflowWithConversation",
        notes = "运行场景化应用接口", response = WorkflowRunRsp.class, tags = {"WorkflowRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = WorkflowRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/runtime/inner/v1/{project_id}/workflows/{workflow_id}/conversations/{conversation_id}",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = {"application/json"}, method = RequestMethod.POST)
    Object runWorkflowWithConversation(@Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "workflow id", required = true, schema = @Schema())
        @PathVariable("workflow_id") String workflowId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @RequestParam(value = "version", required = false) String version,
        @RequestHeader(value = "X-Invoke-Mode", required = false) String invokeMode,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @RequestHeader(value = "X-Invoke-Source", required = false) String invokeSource,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody WorkflowRunReq body,
        @RequestHeader HttpHeaders httpHeaders) {
        return workflowRuntimeController.runWithConversation(projectId, workspaceId, null, workflowId, conversationId,
            version, invokeMode, stream, invokeSource, body, httpHeaders, projectId, workflowId, true,
            RequestContextUtils.getRequestUserDomainId(), executionId);
    }

}
