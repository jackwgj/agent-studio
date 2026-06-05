/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * embedding模型请求体
 *
 */
@Data
@Builder
public class AskEmbeddingReq {
    private String query;

    @JsonProperty("embedding_type")
    private String embeddingType;
}
