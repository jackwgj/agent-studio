
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceData;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelExportEntity {

    @JsonProperty("model_metadata")
    private List<ModelServiceData> modelMetadata;

    @JsonProperty("provider_metadata")
    private com.openjiuwen.studio.agent.manager.entity.ProviderExportMetadata providerMetadata;

}
