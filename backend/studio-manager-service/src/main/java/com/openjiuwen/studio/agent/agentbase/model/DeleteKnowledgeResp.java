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
 * 删除知识库响应实体类
 *
 * @since 2024-04-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteKnowledgeResp {
    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("deleted_count")
    private Integer deletedCount;
}
