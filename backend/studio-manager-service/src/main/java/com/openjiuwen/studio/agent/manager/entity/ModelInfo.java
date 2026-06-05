/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;

import lombok.Builder;
import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
@Builder
public class ModelInfo {
    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_type")
    private String modelType;

    @JsonProperty("hyper_parameters")
    private ModelConfig hyperParameters;

    /**
     * 模型配置文件路径
     */
    @JsonProperty("model_config_path")
    private String modelConfigPath;
}
