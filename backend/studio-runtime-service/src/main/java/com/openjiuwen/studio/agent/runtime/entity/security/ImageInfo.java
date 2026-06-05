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
 * ImageInfo
 *
 */
@Getter
@Setter
@Accessors(chain = true)
public class ImageInfo {
    @JsonProperty("image_name")
    @Size(max = 100)
    private String imageName;

    @JsonProperty("image_url")
    @Size(max = 1024)
    private String imageUrl;

    @JsonProperty("digest_value")
    @Size(max = 1024)
    private String digestValue;
}
