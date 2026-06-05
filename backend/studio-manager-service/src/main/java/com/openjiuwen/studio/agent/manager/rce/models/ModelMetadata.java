/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 模型最大token数量
 *
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class ModelMetadata {
    @JsonProperty("max_tokens")
    private Integer maxTokens = null;

    /**
     * set maxToken
     *
     * @param maxTokens maxTokens
     * @return ModelMetadata
     */
    public ModelMetadata setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }
}
