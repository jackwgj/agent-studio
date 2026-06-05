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
 * 创建知识库响应实体类
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateKnowledgeRepoResp {
    @JsonProperty("repo_id")
    private String repoId;
}
