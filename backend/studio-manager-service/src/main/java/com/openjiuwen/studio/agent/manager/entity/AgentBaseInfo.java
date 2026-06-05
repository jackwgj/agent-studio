/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * agent基本信息
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentBaseInfo {
    @JsonProperty("domain_id")
    private String domainId;

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
     * agent名称
     */
    private String name;

    /**
     * agent状态
     */
    private String status;

    /**
     * agent创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * agent唯一标识
     */
    @JsonProperty("agent_id")
    @Id
    private String agentId;

    @JsonProperty("tenant_id")
    private String tenantId;

}
