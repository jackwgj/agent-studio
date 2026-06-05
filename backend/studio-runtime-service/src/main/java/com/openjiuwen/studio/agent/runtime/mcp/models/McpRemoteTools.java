/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mcp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
public class McpRemoteTools {
    @JsonProperty("count")
    private int count = 0;

    @JsonProperty("tools")
    private List<McpRemoteTool> tools = null;
}
