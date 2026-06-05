/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除知识库请求实体类
 *
 * @since 2024-04-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteKnowledgeRepoReq {
    @JsonProperty("repo_ids")
    private List<String> repoIds;
}
