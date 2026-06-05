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
 * 模型列表查询条件
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelSearchCriteria {
    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_type")
    private String modelType;

    @JsonProperty("model_status")
    private String modelStatus;

    @JsonProperty("page_num")
    private int pageNum;

    @JsonProperty("page_size")
    private int pageSize;
}
