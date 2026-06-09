/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelStrategyExportEntity {
    @JsonProperty("model_export_entity")
    private List<ModelExportEntity> modelExportEntity;

    @JsonProperty("router_strategy_entity_metadata")
    private RouterStrategyEntity routerStrategyEntityMetadata;
}
