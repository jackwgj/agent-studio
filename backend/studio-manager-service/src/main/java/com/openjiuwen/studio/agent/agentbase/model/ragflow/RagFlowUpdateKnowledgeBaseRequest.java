/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagFlowUpdateKnowledgeBaseRequest {
    private String name;

    @JsonProperty("rerank_id")
    private String reRankModel;

    @JsonProperty("parser_id")
    private String parserId;

    @JsonProperty("parser_config")
    private ParserConfig parserConfig;

    @Data
    @Builder
    public static class ParserConfig {
        @JsonProperty("auto_keywords")
        private Integer autoKeywords;

        @JsonProperty("auto_questions")
        private Integer autoQuestions;

        @JsonProperty("chunk_token_num")
        private Integer chunkTokenNum;

        @JsonProperty("delimiter")
        private String delimiter;
    }
}
