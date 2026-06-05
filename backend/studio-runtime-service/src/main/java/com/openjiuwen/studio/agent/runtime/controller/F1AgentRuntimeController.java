/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.AgentRunReq;
import com.openjiuwen.studio.agent.runtime.dto.AgentRunRsp;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.service.F1AgentRuntimeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识型Agent运行接口
 *
 */
@Validated
@RestController
public class F1AgentRuntimeController {
    @Autowired
    private F1AgentRuntimeService f1AgentRuntimeService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private I18nUtil i18nUtil;

    /**
     * 执行知识型Agent，带会话id
     *
     * @param projectId 租户项目id
     * @param agentId agent id
     * @param conversationId 会话id
     * @param stream 是否流式返回
     * @param body AgentRunReq
     * @return Object，非流式返回AgentRunRsp，流式返回Event
     */
    @ApiOperation(value = "run agent", nickname = "runAgentWithConversation", notes = "运行知识型Agent",
            response = AgentRunRsp.class, tags = {"AgentRuntime"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AgentRunRsp.class),
            @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)})
    @RequestMapping(value = "/v1/{project_id}/f1/agents/{agent_id}/conversations/{conversation_id}",
            produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runF1AgentWithConversation(
            @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @Parameter(in = ParameterIn.PATH, description = "项目id",
                    required = true, schema = @Schema()) @PathVariable("project_id") String projectId,
            @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @Parameter(in = ParameterIn.PATH,
                    description = "agent id", required = true, schema = @Schema()) @PathVariable("agent_id") String agentId,
            @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @Parameter(in = ParameterIn.PATH,
                    description = "conversation id", schema = @Schema()) @PathVariable("conversation_id") String conversationId,
            @RequestHeader(value = "stream", required = false) Boolean stream,
            @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody AgentRunReq body) {
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
                .projectId(projectId)
                .agentId(agentId)
                .query(body.getQuery())
                .conversationId(conversationId)
                .type(request.getHeader(Constant.Agent.INVOKE_HEADER_KEY))
                .toolSwitchDict(body.getToolSwitchDict())
                .startTime(System.currentTimeMillis())
                .userId(RequestContextUtils.getRequestUserId())
                .build();
        if (stream == null || stream) {
            return f1AgentRuntimeService.runF1AgentStream(executeParams);
        }
        ErrorRsp errorRsp = new ErrorRsp();
        StudioError errorCode = StudioError.NO_SUPPORT_NON_STREAMING_CALL;
        String code = "openjiuwen." + errorCode.getModule().getSubCode() + errorCode.getCode();
        errorRsp.setErrorCode(code);
        errorRsp.setErrorMsg(i18nUtil.getMessage(code));
        return new ResponseEntity<>(errorRsp, StudioError.NO_SUPPORT_NON_STREAMING_CALL.getHttpStatus());
    }
}
