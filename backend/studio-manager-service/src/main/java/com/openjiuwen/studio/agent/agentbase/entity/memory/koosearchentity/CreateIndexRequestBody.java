/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增记忆索引的请求体
 *
 * @since 2025-10-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateIndexRequestBody {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 使用的向量化模型名称（需要使用KooSearch中存在的模型）
     */
    private String modelName;

    /**
     * 索引的配置
     */
    private MemoryIndexSetting settings;

}
