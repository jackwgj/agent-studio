/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.md;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.run.ChatMessage;
import com.openjiuwen.studio.agent.common.dto.run.Thinking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ChatCompletionRequest extends ModelInvokeBase {
    @Size(max = 200)
    private List<ChatMessage> messages;

    private Boolean stream;

    @JsonProperty("stream_options")
    private Object streamOptions;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("temperature")
    private Double temperature;

    private Object stop;

    private Integer n;

    @JsonProperty("use_beam_search")
    private Boolean useBeamSearch;

    @JsonProperty("presence_penalty")
    private Float presencePenalty;

    @JsonProperty("frequency_penalty")
    private Float frequencyPenalty;

    @JsonProperty("length_penalty")
    private Float lengthPenalty;

    @JsonProperty("ignore_eos")
    private Boolean ignoreEos;

    @Size(max = 1000)
    @JsonProperty("logit_bias")
    private Map<String, Integer> logitBias;

    @Size(max = 1000)
    private String user;

    @Size(max = 1000)
    @JsonProperty("tool_choice")
    private String toolChoice;

    @Size(max = 1000)
    @Valid
    private List<Object> tools;

    private Object data;

    @JsonProperty("with_prompt")
    private Boolean withPrompt;

    @JsonProperty("decode_strategy")
    private Object decodeStrategy;

    private String userId;

    private String scenarioUuid;

    private Map<String, Object> extraInfo;

    private Thinking thinking;

    @JsonProperty("enable_thinking")
    private Boolean enableThinking;

    @JsonProperty("chat_template_kwargs")
    private Map<String, Object> chatTemplateKwargs;
}