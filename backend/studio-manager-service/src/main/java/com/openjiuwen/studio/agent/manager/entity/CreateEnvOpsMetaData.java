/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateEnvOpsMetaData {
    @JsonProperty("environmentName")
    private String environmentName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("vpcId")
    private String vpcId;

    @JsonProperty("subnetId")
    private String subnetId;
}

