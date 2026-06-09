/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * mcp 服务信息
 */
@ApiModel(description = "mcp 服务信息")

@Validated

public class McpServiceToolsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("name_en")
    private String nameEn = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("description_en")
    private String descriptionEn = null;

    @JsonProperty("fcInstanceUrl")
    private String fcInstanceUrl = null;

    @JsonProperty("fcInstanceStatus")
    private String fcInstanceStatus = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("uniqueCode")
    private String uniqueCode = null;

    @JsonProperty("projectId")
    private String projectId = null;

    @JsonProperty("workspaceId")
    private String workspaceId = null;

    @JsonProperty("tools")
    @Valid
    @Size()
    private List<McpServiceToolEntity> tools = null;

    @JsonProperty("deployType")
    private String deployType = null;

    @JsonProperty("createdByUserId")
    private String createdByUserId = null;

    @JsonProperty("tenant_id")
    private String tenantId = null;

    @JsonProperty("last_updated_by_user_id")
    private String lastUpdatedByUserId = null;

    @JsonProperty("deleted")
    private Boolean deleted = false;

    @JsonProperty("created_date")
    private Long createdDate = null;

    @JsonProperty("last_updated_date")
    private Long lastUpdatedDate = null;

    public String getId() {
        return id;
    }

    public McpServiceToolsEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public McpServiceToolsEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public McpServiceToolsEntity setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public McpServiceToolsEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public McpServiceToolsEntity setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
        return this;
    }

    public String getFcInstanceUrl() {
        return fcInstanceUrl;
    }

    public McpServiceToolsEntity setFcInstanceUrl(String fcInstanceUrl) {
        this.fcInstanceUrl = fcInstanceUrl;
        return this;
    }

    public String getFcInstanceStatus() {
        return fcInstanceStatus;
    }

    public McpServiceToolsEntity setFcInstanceStatus(String fcInstanceStatus) {
        this.fcInstanceStatus = fcInstanceStatus;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public McpServiceToolsEntity setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public McpServiceToolsEntity setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public McpServiceToolsEntity setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public McpServiceToolsEntity setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public List<McpServiceToolEntity> getTools() {
        return tools;
    }

    public McpServiceToolsEntity setTools(List<McpServiceToolEntity> tools) {
        this.tools = tools;
        return this;
    }

    public String getDeployType() {
        return deployType;
    }

    public McpServiceToolsEntity setDeployType(String deployType) {
        this.deployType = deployType;
        return this;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public McpServiceToolsEntity setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public McpServiceToolsEntity setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getLastUpdatedByUserId() {
        return lastUpdatedByUserId;
    }

    public McpServiceToolsEntity setLastUpdatedByUserId(String lastUpdatedByUserId) {
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        return this;
    }

    public McpServiceToolsEntity setDeleted(Boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public McpServiceToolsEntity setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public McpServiceToolsEntity setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpServiceToolsEntity {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameEn: ").append(toIndentedString(nameEn)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    descriptionEn: ").append(toIndentedString(descriptionEn)).append("\n");
        sb.append("    fcInstanceUrl: ").append(toIndentedString(fcInstanceUrl)).append("\n");
        sb.append("    fcInstanceStatus: ").append(toIndentedString(fcInstanceStatus)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    uniqueCode: ").append(toIndentedString(uniqueCode)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
        sb.append("    deployType: ").append(toIndentedString(deployType)).append("\n");
        sb.append("    createdByUserId: ").append(toIndentedString(createdByUserId)).append("\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    lastUpdatedByUserId: ").append(toIndentedString(lastUpdatedByUserId)).append("\n");
        sb.append("    deleted: ").append(toIndentedString(deleted)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    lastUpdatedDate: ").append(toIndentedString(lastUpdatedDate)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        McpServiceToolsEntity mcpServiceToolsEntity = (McpServiceToolsEntity) o;
        return Objects.equals(this.id, mcpServiceToolsEntity.id) && Objects.equals(this.name,
            mcpServiceToolsEntity.name) && Objects.equals(this.nameEn, mcpServiceToolsEntity.nameEn) && Objects.equals(
            this.description, mcpServiceToolsEntity.description) && Objects.equals(this.descriptionEn,
            mcpServiceToolsEntity.descriptionEn) && Objects.equals(this.fcInstanceUrl,
            mcpServiceToolsEntity.fcInstanceUrl) && Objects.equals(this.fcInstanceStatus,
            mcpServiceToolsEntity.fcInstanceStatus) && Objects.equals(this.icon, mcpServiceToolsEntity.icon)
            && Objects.equals(this.uniqueCode, mcpServiceToolsEntity.uniqueCode) && Objects.equals(this.projectId,
            mcpServiceToolsEntity.projectId) && Objects.equals(this.workspaceId, mcpServiceToolsEntity.workspaceId)
            && Objects.equals(this.tools, mcpServiceToolsEntity.tools) && Objects.equals(this.deployType,
            mcpServiceToolsEntity.deployType) && Objects.equals(this.createdByUserId,
            mcpServiceToolsEntity.createdByUserId) && Objects.equals(this.tenantId, mcpServiceToolsEntity.tenantId)
            && Objects.equals(this.lastUpdatedByUserId, mcpServiceToolsEntity.lastUpdatedByUserId) && Objects.equals(
            this.deleted, mcpServiceToolsEntity.deleted) && Objects.equals(this.createdDate,
            mcpServiceToolsEntity.createdDate) && Objects.equals(this.lastUpdatedDate,
            mcpServiceToolsEntity.lastUpdatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nameEn, description, descriptionEn, fcInstanceUrl, fcInstanceStatus, icon,
            uniqueCode, projectId, workspaceId, tools, deployType, createdByUserId, tenantId, lastUpdatedByUserId,
            deleted, createdDate, lastUpdatedDate);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
