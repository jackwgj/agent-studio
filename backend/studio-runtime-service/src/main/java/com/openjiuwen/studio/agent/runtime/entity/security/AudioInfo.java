/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * AudioInfo
 *
 */
@Getter
@Setter
@Accessors(chain = true)
public class AudioInfo {
    @JsonProperty("language_code")
    @Size(max = 10)
    private String languageCode;

    @JsonProperty("audio_url")
    @Size(max = 1024)
    private String audioUrl;

    @JsonProperty("digest_value")
    @Size(max = 1024)
    private String digestValue;
}
