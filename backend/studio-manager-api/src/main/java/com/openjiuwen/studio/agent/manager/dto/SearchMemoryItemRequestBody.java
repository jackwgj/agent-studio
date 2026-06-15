/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class SearchMemoryItemRequestBody {
    @NotNull
    private String query;

    @JsonProperty("top_k")
    private Integer topK = 5;

    private Double threshold = 0.5;

    @JsonProperty("memory_ids")
    private List<String> memoryIds;
}
