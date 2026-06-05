/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mcp.models;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class McpRemoteServer {
    private String name;

    private String version;

    private McpRemoteTools tools;
}
