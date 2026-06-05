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
public class BatchCreateResponse {
    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("created_count")
    private Integer createdCount;
}
