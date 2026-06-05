/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.runtime.service.HookTriggerService;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 功能描述
 *
 */
@Validated
@RestController
public class HookTriggerController {
    @Autowired
    private HookTriggerService hookTriggerService;

    /**
     * runHookTrigger
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param triggerId triggerId
     * @param body body
     * @return SseEmitter
     */
    @ApiOperation(value = "事件触发器执行", nickname = "runHookTrigger", notes = "事件触发器执行", response = SseEmitter.class,
        tags = {"AgentRuntime"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "事件触发器执行结果", response = SseEmitter.class),
        @ApiResponse(code = 400, message = "Bad Request 请求错误", response = ErrorRsp.class)})
    @RequestMapping(value = "/v1/{project_id}/agents/{agent_id}/triggers/hook/{trigger_id}",
        produces = {"text/event-stream"}, consumes = {"application/json"}, method = RequestMethod.POST)
    public SseEmitter
        runHookTrigger(@Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @Parameter(in = ParameterIn.PATH,
            description = "租户项目id", required = true, schema = @Schema()) @PathVariable("project_id") String projectId,
            @Parameter(in = ParameterIn.PATH, description = "", required = true,
                schema = @Schema()) @PathVariable("agent_id") String agentId,
            @Parameter(in = ParameterIn.PATH, description = "", required = true,
                schema = @Schema()) @PathVariable("trigger_id") String triggerId,
            @ApiParam(value = "运行请求", required = false) @Valid @RequestBody Map<String, String> body) {
        return hookTriggerService.runHookTrigger(projectId, agentId, triggerId, body);
    }
}
