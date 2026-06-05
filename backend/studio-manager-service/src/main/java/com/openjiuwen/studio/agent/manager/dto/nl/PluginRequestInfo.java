/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Validated
public class PluginRequestInfo {
    @Size(max = 10)
    @JsonProperty("basic_info")
    protected Map<String, Object> basicInfo;

    @Valid
    @Size(max = 200)
    @JsonProperty("tool_info")
    private List<PluginToolInfo> toolInfo;
}
