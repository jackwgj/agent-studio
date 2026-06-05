/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
public class PluginList {
    @Valid
    @Size(max = 200)
    @JsonProperty("plugin_list")
    private List<PluginResource> pluginList;
}
