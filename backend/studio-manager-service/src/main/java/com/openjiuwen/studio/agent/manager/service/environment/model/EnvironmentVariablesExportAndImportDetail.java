/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.environment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.EnvironmentVariable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentVariablesExportAndImportDetail {
    @JsonProperty("environment_id")
    private String environmentId;

    @JsonProperty("variables")
    private List<EnvironmentVariable> environmentVariables;
}
