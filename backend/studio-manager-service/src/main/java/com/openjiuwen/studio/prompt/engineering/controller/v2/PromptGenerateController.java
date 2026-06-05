/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.controller.v2;

import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptBuildRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptFeedbackRequest;
import com.openjiuwen.studio.prompt.engineering.service.v2.PromptOptimizeTaskService;

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
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class PromptGenerateController {

    @Autowired
    private PromptOptimizeTaskService promptOptimizeTaskService;

    @ApiOperation(value = "提示词生成", nickname = "promptGenerate", notes = "提示词生成", response = Object.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "创建的prompt", response = Object.class)})
    @RequestMapping(value = "/v2/{project_id}/agent-builder/prompt/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE,
        consumes = {"application/json"}, method = RequestMethod.POST)
    public Flux<String> promptBuilder(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
    @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
    @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId,
        @NotNull @ApiParam(value = "生成prompt请求体", required = true) @Valid @RequestBody PromptBuildRequest body) {
        return promptOptimizeTaskService.generatePrompt(projectId, workspaceId, body);
    }

    @ApiOperation(value = "提示词反馈优化", nickname = "promptFeedbackGenerate", notes = "提示词反馈优化",
        response = Object.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "创建的prompt", response = Object.class)})
    @RequestMapping(value = "/v2/{project_id}/agent-builder/prompt/optimize_feedback",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = {"application/json"}, method = RequestMethod.POST)
    public Flux<String> optimizeFeedback(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
    @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
    @PathVariable("project_id") String projectId,
        @NotNull @ApiParam(value = "空间ID", required = true) @RequestParam(value = "workspace_id", required = true)
        String workspaceId, @NotNull @ApiParam(value = "生成prompt请求体", required = true) @Valid @RequestBody
    PromptFeedbackRequest body) {
        return promptOptimizeTaskService.optimizeFeedback(projectId, workspaceId, body);
    }
}
