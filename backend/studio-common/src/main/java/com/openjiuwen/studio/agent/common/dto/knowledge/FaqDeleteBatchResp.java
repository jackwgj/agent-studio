/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量删除FAQ响应体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqDeleteBatchResp {
    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("deleted_count")
    private Integer deletedCount;
}
