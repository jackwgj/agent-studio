/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
public class PluginResource {
    @NotNull
    @JsonProperty("plugin_id")
    @Length(max = 200)
    private String pluginId;

    @JsonProperty("plugin_display_name")
    @Length(max = 200)
    private String pluginDisplayName;

    @JsonProperty("plugin_chinese_name")
    @Length(max = 200)
    private String pluginChinesName;

    @JsonProperty("plugin_desc")
    @Length(max = 200)
    private String pluginDesc;

    @Valid
    @NotNull
    @JsonProperty("request_info")
    private PluginRequestInfo requestInfo;

    @Size(max = 200)
    @Valid
    @JsonProperty("input_schema")
    private List<PluginInput> inputSchema;

    @Size(max = 200)
    @Valid
    @JsonProperty("output_schema")
    private List<PluginOutput> outputSchema;

    @JsonProperty("last_version_id")
    @Length(max = 200)
    private String lastVersionId;
}
