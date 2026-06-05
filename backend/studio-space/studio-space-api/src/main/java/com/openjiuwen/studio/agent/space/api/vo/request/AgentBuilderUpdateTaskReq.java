/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.ValidChatString;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 修改任务请求
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderUpdateTaskReq {
    @NotBlank
    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("task_id")
    private String taskId;

    @Size(max = 64)
    @ValidChatString
    private String name;
}