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
 * 批量创建任务返回体
 *
 * @since 2025-04-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeBaseStatistics {

    @JsonProperty("knowledge_base_connection_id")
    private String knowledgeBaseConnectionId;

    /**
     * 在系统中已经使用的知识库个数
     */
    @JsonProperty("repo_num")
    private Integer repoNum;

    /**
     * 在系统中已经使用的知识库个数
     */
    @JsonProperty("total_num")
    private Integer totalNum;

    public String toString() {
        return "[knowledge base connection id: " + knowledgeBaseConnectionId + "][capacity: " + totalNum + "][used: "
            + repoNum + "]";
    }
}
