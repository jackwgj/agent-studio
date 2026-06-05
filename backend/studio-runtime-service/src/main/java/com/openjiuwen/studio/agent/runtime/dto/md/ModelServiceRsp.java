/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ModelServiceRsp {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("provider_id")
    private String providerId = null;

    @JsonProperty("service_name")
    private String serviceName = null;

    @JsonProperty("service_key")
    private String serviceKey = null;

    @JsonProperty("model_name")
    private String modelName = null;
}
