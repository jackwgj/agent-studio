package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName(value = "ws_mcp_service_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class McpServiceEntity extends BaseProperties {

    /**
     * MCP service 主键ID UUID
     */
    private String id;

    /**
     * MCP server 名称
     */
    private String name;

    /**
     * MCP server 英文名称
     */
    private String nameEn;

    /**
     * MCP server 描述
     */
    private String description;

    /**
     * MCP server 英文描述
     */
    private String descriptionEn;


    /**
     * 托管的MPC服务函数实例的URL
     */

    private String fcInstanceUrl;


    /**
     * 托管的MPC服务函数所在的Region，暂时可能用不到
     */
    private String fcRegion;


    /**
     * ICON 图标
     */
    private String icon;

    /**
     * server配置
     */
    private String serverConfig;


    /**
     * 安装方式 sse|stdio|streamableHttp
     */
    private String deployType;
}
