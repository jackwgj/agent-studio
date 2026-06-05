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
 * 创建知识库响应实体类
 *
 * @since 2024-04-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateKnowledgeRepoResp {
    @JsonProperty("repo_id")
    private String repoId;
}
