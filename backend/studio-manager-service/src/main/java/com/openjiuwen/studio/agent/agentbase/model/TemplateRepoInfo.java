
/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板知识库结构体
 *
 * @since 2025-04-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRepoInfo {

    @JsonProperty("temp_repo_id")
    private Integer tempRepoId;

    @JsonProperty("embedding_model")
    private String embeddingModel;

    @JsonProperty("rerank_model")
    private String rerankModel;

    /**
     * 模板知识库已使用量
     */
    @JsonProperty("used")
    private Integer used;

    /**
     * 模板知识库总容量
     */
    @JsonProperty("capacity")
    private Integer capacity;

    public String queryKey() {
        return String.format("%s:%s", embeddingModel, rerankModel);
    }
}
