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
 * 功能描述 高代码智能体session会话数据库实体
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_agent_code_session")
public class CodeAgentSession {
    /**
     * 主键ID
     */
    @JsonProperty("id")
    @Id
    private String id;

    /**
     * agent唯一标识
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * session id
     */
    @JsonProperty("session_id")
    private String sessionId;

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
     * agent创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * agent更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;
}
