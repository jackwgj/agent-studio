/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity.md;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelServiceProviderDetail extends ModelServiceProvider {
    /**
     * 数据库model_service_provider表 中不存在字段，联合查询使用
     */
    private String authMetaIds;

    /**
     * 数据库model_service_provider表 中不存在字段，联合查询使用
     */
    private String authTypes;

    /**
     * 数据库model_service_provider表 中不存在字段，联合查询使用
     */
    private String authIds;

    /**
     * 数据库model_service_provider表 中不存在字段，联合查询使用
     */
    private int authConfigNum;

    private int authExistNum;

    /**
     * 数据库model_service_provider表 中不存在字段，联合查询使用
     */
    private int authMetadataNum;

    private String source;
}
