/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ImageInfo {
    @JsonProperty("image_url")
    @JSONField(name = "image_url")
    private MediaUrl imageUrl;

    @JsonProperty("type")
    @JSONField(name = "type")
    private String type;
}
