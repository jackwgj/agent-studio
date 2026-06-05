/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * MCP server 列表信息
 *
 */
@Data
@Accessors(chain = true)
public class McpServiceFromProject {
    /**
     * MCP service 主键ID UUID
     */
    @JsonProperty("id")
    public String id;

    /**
     * MCP server 名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * MCP server 英文名称
     */
    @JsonProperty("name_en")
    public String nameEn;

    /**
     * MCP server 描述
     */
    @JsonProperty("description")
    public String description;

    /**
     * MCP server 英文描述
     */
    @JsonProperty("description_en")
    public String descriptionEn;

    /**
     * 拉包类型 NPX|UVX
     */
    @JsonProperty("org_type")
    public String orgType;

    /**
     * 安装方式 sse|stdio|streamableHttp
     */
    @JsonProperty("deploy_type")
    public String deployType;

    /**
     * server配置
     */
    @JsonProperty("server_config")
    public String serverConfig;

    /**
     * 托管的MPC服务函数实例在FG侧的状态
     */
    @JsonProperty("fc_instance_status")
    public String fcInstanceStatus;

    /**
     * 托管的MPC服务函数实例的URL
     */
    @JsonProperty("fc_instance_url")
    public String fcInstanceUrl;

    /**
     * 唯一编码，用于项目导入重复性判断
     */
    @JsonProperty("unique_code")
    public String uniqueCode;

    /**
     * mcp服务模版id
     */
    @JsonProperty("server_id")
    public String serverId;

    @JsonProperty("created_date")
    public Timestamp createdDate;

    @JsonProperty("last_updated_date")
    public Timestamp lastUpdatedDate;

}
