/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagFlowCreateTaskRequest {

    @JsonProperty("doc_ids")
    private List<String> docIds;

    private int run;

    private Boolean delete;
}
