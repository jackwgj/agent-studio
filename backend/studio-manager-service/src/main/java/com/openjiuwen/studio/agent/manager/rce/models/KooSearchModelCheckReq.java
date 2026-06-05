/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * kooSearch模型健康检查请求体
 *
 */
@Data
@Builder
public class KooSearchModelCheckReq {
    @JsonProperty("deployment_id")
    private String deploymentId;

    @JsonProperty("model_type")
    private String modelType;

    @JsonProperty("model_config_path")
    private String modelConfigPath;

    private Object data;
}
