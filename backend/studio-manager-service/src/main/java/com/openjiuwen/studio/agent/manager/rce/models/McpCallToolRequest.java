/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * MCP工具运行请求体
 *
 */
@Builder
@Data
public class McpCallToolRequest {
    @JsonProperty("url")
    private String url;

    @JsonProperty("name")
    private String name;

    @JsonProperty("params")
    private Map<String, Object> params;

    @JsonProperty("auth")
    private AuthInfo authInfo;
}
