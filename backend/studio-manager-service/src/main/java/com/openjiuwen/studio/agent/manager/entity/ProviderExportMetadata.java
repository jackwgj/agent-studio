/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProviderExportMetadata {
    @JsonProperty("provider_auth_metadata")
    private ProviderAuthMetadata providerAuthMetadata;
    @JsonProperty("model_service_provider_metadata")
    private ModelServiceProvider modelServiceProviderMetadata;
    @JsonProperty("provider_auth_data")
    private ProviderAuthData providerAuthData;
}
