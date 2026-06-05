/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class SttConfig {
    @NotNull(message = "audio_format must not be null")
    @JsonProperty("audio_format")
    @JSONField(name = "audio_format")
    private String audioFormat;

    @NotNull(message = "property cant be null, chinese_16k_general is best")
    private String property;

    @JsonProperty("add_punc")
    @JSONField(name = "add_punc")
    private String addPunctuation;
}
