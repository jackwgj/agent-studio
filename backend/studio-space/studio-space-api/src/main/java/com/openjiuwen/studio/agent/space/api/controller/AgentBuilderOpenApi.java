/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.controller;

import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderChatReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderCreateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderMessageListReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderOperateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderQueryTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentBuilderTaskListRsp;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.DRMessageVo;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import javax.annotation.Nullable;

/**
 * openapi接口类
 */
@Validated
@RequestMapping("/agentspace/openapi/v1/agent-builder")
public interface AgentBuilderOpenApi {
    @PostMapping(value = "/task/create")
    BaseResp<String> createTask(@RequestBody @Valid AgentBuilderCreateTaskReq req);

    @PostMapping(value = "/task/list")
    BaseResp<AgentBuilderTaskListRsp> listTask(@RequestBody @Valid AgentBuilderQueryTaskReq req);

    /**
     * 复杂任务规划对话，需要先创建task
     */
    @PostMapping(value = "/chat")
    SseEmitter chat(@Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
    @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema()) @RequestParam(value = "workspace_id")
    String workspaceId, @RequestBody @Valid AgentBuilderChatReq req);

    @PostMapping(value = "/task/operate")
    BaseResp<String> operateTask(@RequestBody @Valid AgentBuilderOperateTaskReq req);

    @PostMapping(value = "/message/list")
    BaseResp<List<DRMessageVo>> listMessage(@RequestBody @Valid AgentBuilderMessageListReq req);

    @GetMapping(value = "/file/download")
    ResponseEntity<InputStreamResource> downloadFile(
        @RequestParam("task_id") @Valid @Size(max = 64) @ValidNormalString String taskId,
        @RequestParam("file_id") @Valid @Size(max = 64) @ValidNormalString String fileId,
        @RequestParam("target_format") @Nullable @Valid @Pattern(regexp = "^(pdf|docx)$") String targetFormat);
}
