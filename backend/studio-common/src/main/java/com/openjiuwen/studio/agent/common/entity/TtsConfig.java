/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class TtsConfig {
    @JsonProperty("audio_format")
    @JSONField(name = "audio_format")
    private String audioFormat;

    @JsonProperty("sample_rate")
    @JSONField(name = "sample_rate")
    private String sampleRate; // 采样率支持16000和8000，采样率越高音频质量越高

    private String property;

    @Range(min = -500, max = 500)
    private Integer speed;

    @Range(min = 0, max = 100)
    private Integer volume;
}
