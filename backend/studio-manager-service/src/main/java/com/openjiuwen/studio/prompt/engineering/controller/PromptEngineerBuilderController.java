/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.agent.common.dto.prompt.PromptBuilderRsp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBuilderReq;
import com.openjiuwen.studio.prompt.engineering.service.PromptEngineerBuilderService;

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
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 功能描述
 *
 */
@Validated
@RestController
public class PromptEngineerBuilderController {
    @Autowired
    private PromptEngineerBuilderService promptEngineerBuilderService;

    /**
     * promptBuilder
     *
     * @param projectId projectId
     * @param body body
     * @return SseEmitter
     */
    @ApiOperation(value = "提示词生成", nickname = "promptBuilder", notes = "提示词生成", response = PromptBuilderRsp.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "创建的prompt", response = PromptBuilderRsp.class)})
    @RequestMapping(value = "/v1/{project_id}/agent-builder/prompt/generate", produces = {"text/event-stream"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    public SseEmitter promptBuilder(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64) @Parameter(in = ParameterIn.PATH,
            description = "租户项目id", required = true, schema = @Schema()) @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "生成prompt请求体", required = true) @Valid @RequestBody PromptBuilderReq body) {
        return promptEngineerBuilderService.generatePromptTemplate(projectId, body);
    }
}
