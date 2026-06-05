/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.api.vo.request.ChatInputRequest;
import com.openjiuwen.studio.agent.space.api.vo.request.ChatTemplateRequest;
import com.openjiuwen.studio.agent.space.common.validator.IntegerEnumValue;
import com.openjiuwen.studio.agent.space.common.validator.ValidChatString;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;
import com.openjiuwen.studio.agent.space.common.validator.ValidStringList;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;

/**
 * 调度任务 和 运行时 交互所用数据传输类
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderDispatchVo {
    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("task_id")
    private String taskId;

    @Size(max = 10000)
    @ValidChatString
    @JsonProperty("query")
    private String query;

    @Nullable
    @Valid
    @JsonProperty("inputs")
    private ChatInputRequest inputs;

    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("answer_id")
    private String answerId;

    @JsonProperty("files")
    @ValidStringList(maxSize = 10)
    private List<String> files;

    /**
     * 运行方式
     * 0：AgentSpace；1：AgentWeb
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("run_type")
    private int runType = 0;

    /**
     * 运行的agent
     * 由AgentWeb发起的请求，存储的是shortCode
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 当前任务状态
     */
    @JsonProperty("task_status")
    private int taskStatus;

    /**
     * 当前任务状态
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * dr 模板文件地址
     */
    @JsonProperty("dr_template")
    private ChatTemplateRequest drTemplate;

    /**
     * 开始时间
     */
    @JsonProperty("start_time")
    private Timestamp startTime;
}
