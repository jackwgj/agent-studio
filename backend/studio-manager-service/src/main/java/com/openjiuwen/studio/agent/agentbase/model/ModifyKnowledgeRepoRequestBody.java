/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileExtract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改知识库请求体
 *
 * @since 2024-11-12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModifyKnowledgeRepoRequestBody {
    private String name;

    @JsonProperty("rerank_model")
    private String rerankModel;

    @JsonProperty("file_extract")
    private FileExtract fileExtract;
}
