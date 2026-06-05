/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 删除消息请求类
 */
@Data
@Accessors(chain = true)
public class AgentBuilderDeleteMessageReq {
    @NotBlank
    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("task_id")
    private String taskId;

    @NotBlank
    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("message_id")
    private String messageId;
}