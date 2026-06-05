/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Set;

@Data
public class RagFlowDeleteDataSetRequest {
    @JsonProperty("dataset_ids")
    private Set<String> fileIds;
}
