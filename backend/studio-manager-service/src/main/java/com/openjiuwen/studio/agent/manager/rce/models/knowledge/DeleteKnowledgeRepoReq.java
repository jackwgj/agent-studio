/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除知识库请求实体类
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteKnowledgeRepoReq {
    @JsonProperty("repo_ids")
    @Size(max = 1000)
    private List<String> repoIds;
}
