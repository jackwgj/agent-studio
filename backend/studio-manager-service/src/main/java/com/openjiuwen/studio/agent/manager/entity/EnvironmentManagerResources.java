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
public class EnvironmentManagerResources {

    /**
     * 创建环境的参数
     */
    @JsonProperty("ops_metadata")
    private CreateEnvOpsMetaData opsMetadata;

    @JsonProperty("vpc_cidr")
    private String vpcCidr;

    @JsonProperty("vpc_name")
    private String vpcName;

    @JsonProperty("subnet_cidr")
    private String subnetCidr;

    @JsonProperty("subnet_name")
    private String subnetName;
}

