/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改知识库请求体
 *
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
