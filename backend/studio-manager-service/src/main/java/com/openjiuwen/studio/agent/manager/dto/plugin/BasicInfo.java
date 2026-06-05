/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicInfo {

    @JsonProperty("host")
    private String host;

    @JsonProperty("path")
    private String path ;

    @JsonProperty("protocol")
    private String protocol ;

    @JsonProperty("input_schema")
    private String inputSchema;
}
