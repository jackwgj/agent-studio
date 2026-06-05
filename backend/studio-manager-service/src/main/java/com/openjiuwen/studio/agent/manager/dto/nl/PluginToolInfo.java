/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@Validated
public class PluginToolInfo {
    @NotNull
    @JsonProperty("tool_id")
    @Length(max = 200)
    private String toolId;

    @NotNull
    @JsonProperty("tool_display_name")
    @Length(max = 200)
    private String toolDisplayName;

    @JsonProperty("tool_chinese_name")
    @Length(max = 200)
    private String toolChinesName;

    @JsonProperty("tool_desc")
    @Length(max = 200)
    private String toolDesc;

    @JsonProperty("method")
    @Length(max = 200)
    private String method;

    @NotNull
    @JsonProperty("path")
    @Length(max = 200)
    private String path;
}
