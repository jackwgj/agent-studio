/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RagFlowCreateChunkRequest {

    @JsonProperty("available_int")
    private Integer available = 1;

    @JsonProperty("content_with_weight")
    private String content;

    @JsonProperty("title")
    private String title;
}
