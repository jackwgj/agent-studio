/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识检索请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssistantKnowledgeSearchReq {
    @JsonProperty("knowledge_repo_ids")
    @Size(max = 1000)
    private List<String> knowledgeRepoIds;

    private String query;

    @JsonProperty("knowledge_retrieve_policy")
    private KnowledgeRetrievePolicy knowledgeRetrievePolicy;
}
