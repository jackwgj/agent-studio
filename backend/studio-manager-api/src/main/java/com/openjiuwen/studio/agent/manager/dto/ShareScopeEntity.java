/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ShareScopeEntity
 */

@Validated

public class ShareScopeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("resource_id")
    private String resourceId = null;

    @JsonProperty("resource_type")
    private String resourceType = null;

    @JsonProperty("workspace_id")
    private String workspaceId = null;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("tenant_id")
    private String tenantId = null;

    public String getId() {
        return id;
    }

    public ShareScopeEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ShareScopeEntity setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public ShareScopeEntity setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ShareScopeEntity setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public ShareScopeEntity setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public ShareScopeEntity setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ShareScopeEntity {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
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
        ShareScopeEntity shareScopeEntity = (ShareScopeEntity) o;
        return Objects.equals(this.id, shareScopeEntity.id) && Objects.equals(this.resourceId,
            shareScopeEntity.resourceId) && Objects.equals(this.resourceType, shareScopeEntity.resourceType)
            && Objects.equals(this.workspaceId, shareScopeEntity.workspaceId) && Objects.equals(this.projectId,
            shareScopeEntity.projectId) && Objects.equals(this.tenantId, shareScopeEntity.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resourceId, resourceType, workspaceId, projectId, tenantId);
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
