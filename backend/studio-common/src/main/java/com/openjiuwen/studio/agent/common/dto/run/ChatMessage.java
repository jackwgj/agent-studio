/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ChatMessage {
    /**
     * Must be either 'system', 'user', 'assistant' or 'function'.<br>
     */
    @Size(max = 1000)
    @NotNull(message = "Parameter 'role' must be not null, please check and try again later!")
    private String role;

    @JsonInclude() // content should always exist in the call, even if it is null
    private Object content;

    @JsonProperty("reasoning_content")
    private String reasoningContent;

    // name is optional, The name of the author of this message. May contain a-z,
    // A-Z, 0-9, and underscores, with a maximum length of 64 characters.
    @Size(max = 1000)
    private String name;

    @Valid
    @JSONField(name = "tool_calls")
    @JsonProperty("tool_calls")
    private List<Object> toolCalls;

    @Size(max = 1000)
    @JSONField(name = "tool_call_id")
    @JsonProperty("tool_call_id")
    private String toolCallId;
}
