/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 智能体应用信息。
 */
@ApiModel(description = "智能体应用信息。")

@Validated

public class AgentApplicationInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("workspace_id")
    private String workspaceId = null;

    @JsonProperty("workspace_name")
    private String workspaceName = null;

    @JsonProperty("updated_at")
    private String updatedAt = null;

    @JsonProperty("prologue")
    private String prologue = null;

    public String getId() {
        return id;
    }

    public AgentApplicationInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AgentApplicationInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public AgentApplicationInfo setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AgentApplicationInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public AgentApplicationInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public AgentApplicationInfo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public AgentApplicationInfo setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
        return this;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public AgentApplicationInfo setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public String getPrologue() {
        return prologue;
    }

    public AgentApplicationInfo setPrologue(String prologue) {
        this.prologue = prologue;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentApplicationInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    workspaceName: ").append(toIndentedString(workspaceName)).append("\n");
        sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
        sb.append("    prologue: ").append(toIndentedString(prologue)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentApplicationInfo agentApplicationInfo = (AgentApplicationInfo) o;
        return Objects.equals(this.id, agentApplicationInfo.id) && Objects.equals(this.name, agentApplicationInfo.name)
            && Objects.equals(this.icon, agentApplicationInfo.icon) && Objects.equals(this.description,
            agentApplicationInfo.description) && Objects.equals(this.type, agentApplicationInfo.type) && Objects.equals(
            this.workspaceId, agentApplicationInfo.workspaceId) && Objects.equals(this.workspaceName,
            agentApplicationInfo.workspaceName) && Objects.equals(this.updatedAt, agentApplicationInfo.updatedAt)
            && Objects.equals(this.prologue, agentApplicationInfo.prologue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, description, type, workspaceId, workspaceName, updatedAt, prologue);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
