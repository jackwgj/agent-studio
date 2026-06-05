/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 大模型节点模型配置
 *
 */
@Data
public class WorkflowLLMConfig {
    private LLMParamConfig config;

    private String name;

    @Data
    public static class LLMParamConfig {
        @JsonProperty("model_version")
        private String modelVersion;

        @JsonProperty("model_type")
        private String modelType;

        private String model;

        private double temperature;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        @JsonProperty("top_p")
        private double topP;
    }
}
