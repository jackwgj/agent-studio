/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagFlowSearchTextRequest {
    @JsonProperty("question")
    private String question;

    @JsonProperty("knowledge_base_ids")
    private List<String> knowledgeBaseIds;

    @JsonProperty("page")
    private Integer pageNum;

    @JsonProperty("size")
    private Integer pageSize;

}
