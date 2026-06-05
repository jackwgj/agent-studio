/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class PluginOutput {
    @NotNull
    @JsonProperty("tool_id")
    @Length(max = 200)
    private String toolId;

    @NotNull
    @JsonProperty("output_schema")
    @Length(max = 20000)
    private String outputSchema;
}
