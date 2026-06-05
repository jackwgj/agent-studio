/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述 知识库对象
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeRepo {
    @JsonProperty("knowledge_repo_id")
    private String knowledgeRepoId;

    @JsonProperty("display_name")
    private String displayName;
}
