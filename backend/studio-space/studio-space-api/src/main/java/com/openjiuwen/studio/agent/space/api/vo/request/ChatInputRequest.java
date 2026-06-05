/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.ValidChatString;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 重新生成结果请求
 */
@Data
@Accessors(chain = true)
public class ChatInputRequest implements Serializable {
    @Size(max = 20000)
    @ValidChatString
    private String message;

    @JsonProperty("interrupt_feedback")
    private String interruptFeedback;

    @JsonProperty("template")
    private ChatTemplateRequest template;
}
