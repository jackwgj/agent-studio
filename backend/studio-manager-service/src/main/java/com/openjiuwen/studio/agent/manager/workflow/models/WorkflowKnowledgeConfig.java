/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;

import lombok.Data;

import java.util.List;

/**
 * 知识库组件参数，用于json解析
 *
 */
@Data
public class WorkflowKnowledgeConfig {
    @JsonProperty("knowledge_repo_ids")
    private List<String> knowledgeRepoIds;

    @JsonProperty("web_search_enabled")
    private Boolean webSearchEnabled;

    @JsonProperty("search_mode")
    private KnowledgeRetrievePolicy.SearchModeEnum searchMode;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("recall_threshold")
    private float recallThreshold;
}
