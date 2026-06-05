/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class StsTextResp {

    @JsonProperty("message")
    private String message;
}
