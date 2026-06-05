/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.service.mcp.common.validator.ValidMcpName;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * MCP server 列表信息
 *
 */
@Data
@Accessors(chain = true)
public class ImportMcpServiceDependencyCheck {
    /**
     * MCP service 主键ID UUID
     */
    @JsonProperty("id")
    private String id;

    /**
     * MCP server 名称
     */
    @JsonProperty("name")
    @ValidMcpName
    private String name;

    /**
     * MCP server 英文名称
     */
    @JsonProperty("name_en")
    private String nameEn;

    /**
     * MCP server 描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * MCP server 英文描述
     */
    @JsonProperty("description_en")
    private String descriptionEn;

    /**
     * 拉包类型 NPX|UVX
     */
    @JsonProperty("org_type")
    private String orgType;

    /**
     * 安装方式 sse|stdio|streamableHttp
     */
    @JsonProperty("deploy_type")
    private String deployType;

    /**
     * 唯一编码，用于项目导入重复性判断
     */
    @JsonProperty("unique_code")
    private String uniqueCode;

    @JsonProperty("created_date")
    private Timestamp createdDate;

    @JsonProperty("last_updated_date")
    private Timestamp lastUpdatedDate;

    /**
     * 检查是否重复  true 重复， false 不重复
     */
    @JsonProperty("is_exist")
    boolean isExist = true;

    /**
     * 检查是否可以覆盖mcp  false 不能覆盖，只能新增和跳过， true 可以覆盖、新增、跳过
     */
    @JsonProperty("is_covered")
    boolean isCovered = true;

}
