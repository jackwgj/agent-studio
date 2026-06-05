/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VideoInfo {
    @JsonProperty("video_url")
    private MediaUrl videoUrl;

    @JsonProperty("type")
    private String type;
}
