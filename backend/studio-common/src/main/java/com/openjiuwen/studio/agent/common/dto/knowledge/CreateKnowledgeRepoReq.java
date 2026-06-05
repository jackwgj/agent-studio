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
 * 创建知识库请求实体类
 *
 * @since 2024-04-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateKnowledgeRepoReq {

    private String name;

    private String detail;

    @JsonProperty("embedding_model")
    private String embeddingModel;

    @JsonProperty("rerank_model")
    private String reRankModel;

    @JsonProperty("nlp_model")
    private String nlpModel;

    @JsonProperty("repo_type")
    private String repoType;

    @JsonProperty("repo_index_config")
    private RepoIndexConfig repoIndexConfig;

    @JsonProperty("file_extract")
    private FileExtract fileExtract;

    @JsonProperty("search_plan_category_ids")
    private List<String> searchPlanCategoryIds;

    @JsonProperty("language_id")
    private String languageId;

    @JsonProperty("cache_enabled")
    private boolean cacheEnabled;

    @JsonProperty("answer_reference_enabled")
    private boolean answerReferenceEnabled;

    @JsonProperty("answer_image_reference_enabled")
    private boolean answerImageReferenceEnabled;

    @JsonProperty("session_config")
    private SessionConfig sessionConfig;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionConfig {
        @JsonProperty("similarity_threshold")
        private Float similarityThreshold;

        @JsonProperty("answer_select_policy")
        private String answerSelectPolicy;

        private Eviction eviction;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Eviction {
            private String policy;

            private Long ttl;

            @JsonProperty("hit_count_threshold")
            private Long hitCountThreshold;
        }
    }
}
