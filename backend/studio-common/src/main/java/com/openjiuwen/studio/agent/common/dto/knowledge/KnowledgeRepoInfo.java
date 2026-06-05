/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述
 *
 * @since 2024-04-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeRepoInfo {
    private String id;

    private String name;

    private String detail;

    private String status;

    @JsonProperty("create_user")
    private String createUser;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("repo_type")
    private String repoType;

    // 此模板知识库下已经使用的共享知识库
    @JsonProperty("share_repo_used")
    private Integer shareRepoUsed;

    // 此模板知识库下剩余的共享知识库配额
    @JsonProperty("share_repo_quota")
    private Integer shareRepoQuota;

    @JsonProperty("update_time")
    private String updateTime;

    private List<KnowledgeRepoFieldSchema> fields;

    @JsonProperty("embedding_model")
    private String embeddingModel;

    @JsonProperty("rerank_model")
    private String rerankModel;

    @JsonProperty("nlp_model")
    private String nlpModel;

    @JsonProperty("file_extract")
    private FileExtract fileExtract;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KnowledgeRepoFieldSchema {
        private String name;

        @JsonProperty("field_type")
        private String fieldType;

        @JsonProperty("name_zh")
        private String nameZh;
    }
}
