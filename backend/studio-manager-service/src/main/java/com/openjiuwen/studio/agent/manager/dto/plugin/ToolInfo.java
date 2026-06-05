/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolInfo {

    @JsonProperty("tool_id")
    private String toolId;

    @JsonProperty("function_id")
    private String functionId;

    @JsonProperty("tool_display_name")
    private String toolDisplayName;

    @JsonProperty("tool_chinese_name")
    private String toolChineseName;

    @JsonProperty("tool_desc")
    private String toolDesc;

    @JsonProperty("tool_desc_en")
    private String toolDescEn;

    @JsonProperty("url")
    private String url;

    @JsonProperty("method")
    private String method;

    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("path_params")
    private String pathParams;

    @JsonProperty("path")
    private String path;

    @JsonProperty("test_status")
    private String testStatus;
}
