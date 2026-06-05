/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建任务返回体
 *
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
