/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库引用对象
 *
 */
@Data
@Getter
@Setter
public class KnowledgeReference {
    @JsonProperty("knowledge_repo_id")
    private String knowledgeRepoId;

    private String type;
}
