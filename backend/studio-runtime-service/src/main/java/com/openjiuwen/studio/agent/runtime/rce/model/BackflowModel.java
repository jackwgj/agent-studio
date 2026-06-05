/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述 回流数据模型信息
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackflowModel {
    @JsonProperty("deployment_id")
    private String deploymentId;

    private String name;

    private String version;
}
