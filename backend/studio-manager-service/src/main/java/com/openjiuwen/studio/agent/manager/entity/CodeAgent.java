/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 功能描述 高代码智能体数据库实体
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_agent_code")
public class CodeAgent {
    /**
     * agent唯一标识
     */
    @JsonProperty("id")
    @Id
    private String id;

    /**
     * agent名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * agent描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * icon图标名称
     */
    @JsonProperty("icon_name")
    private String iconName;

    /**
     * agent类型
     */
    @JsonProperty("type")
    private String type;

    /**
     * agent状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * Relay Agent沙箱urn
     */
    @JsonProperty("builder_sandbox_urn")
    private String builderSandboxUrn;

    /**
     * CodeInterpreter沙箱urn
     */
    @JsonProperty("dev_sandbox_urn")
    private String devSandboxUrn;

    /**
     * 溯源ID
     */
    @JsonProperty("trace_id")
    private String traceId;

    /**
     * 租户唯一标识
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 项目空间ID
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 项目空间ID
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * 创建人id
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 创建人名称
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * agent创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * agent更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * agent发布时间
     */
    @JsonProperty("published_on")
    private Date publishedOn;

    /**
     * 自动生成标识,(0=否，1=是)
     */
    @JsonProperty("auto_gen_flag")
    private Integer autoGenFlag;
}
