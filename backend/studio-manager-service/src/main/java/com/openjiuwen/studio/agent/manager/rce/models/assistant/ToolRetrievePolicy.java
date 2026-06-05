/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 工具检索策略
 *
 */
@Data
@Getter
@Setter
public class ToolRetrievePolicy {
    private Boolean enabled;

    private String type;

    @JsonProperty("rewriting_model")
    private String rewritingModel;

    @JsonProperty("rewriting_model_endpoint")
    private String rewritingModelEndpoint;

    @JsonProperty("rewriting_model_name")
    private String rewritingModelName;

    @JsonProperty("top_n")
    private Integer topN;

    @JsonProperty("embedding_recall_threshold")
    private Double embeddingRecallThreshold;

    @JsonProperty("embedding_recall_break")
    private Boolean embeddingRecallBreak;
}
