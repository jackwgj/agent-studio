/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * MCP服务配置解析, 用于debug接口和生成agent接口
 *
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpServerRuntimeVo {

    private String id;

    /**
     * mcp名称
     */
    private String name;

    /**
     * mcp类型 内置 自定义
     */
    private String type;

    /**
     * mcp描述信息
     */
    private String description;

    /**
     * mcp启动指令
     */
    private String command;

    private String baseUrl;

    private List<String> args;

    /**
     * MCP环境变量
     */
    private Map<String, String> env;

    private Map<String, String> headers;

    private String icon;
}
