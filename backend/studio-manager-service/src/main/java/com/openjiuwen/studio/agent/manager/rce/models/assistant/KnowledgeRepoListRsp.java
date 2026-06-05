/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.entity.KnowledgeRepoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Assistant关联知识库列表对象
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeRepoListRsp {
    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("max_size")
    private Long maxSize;

    @JsonProperty("total_size")
    private Long totalSize;

    @JsonProperty("knowledge_repo_list")
    private List<KnowledgeRepo> knowledgeRepoList;

    @JsonProperty("knowledge_repo_entity_list")
    private List<KnowledgeRepoEntity> knowledgeRepoEntityList;
}
