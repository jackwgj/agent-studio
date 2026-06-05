/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 工作空间外部映射表实体。
 */
@ApiModel(description = "工作空间外部映射表实体。")

@Validated

public class WorkspaceMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("workspaceId")
    @Length(max = 64)
    private String workspaceId = null;

    @JsonProperty("externalMappingId")
    @Length(max = 128)
    private String externalMappingId = null;

    @JsonProperty("externalExtension")
    private String externalExtension = null;

    @JsonProperty("externalMappingSource")
    private Integer externalMappingSource = null;

    public String getId() {
        return id;
    }

    public WorkspaceMapping setId(String id) {
        this.id = id;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceMapping setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getExternalMappingId() {
        return externalMappingId;
    }

    public WorkspaceMapping setExternalMappingId(String externalMappingId) {
        this.externalMappingId = externalMappingId;
        return this;
    }

    public String getExternalExtension() {
        return externalExtension;
    }

    public WorkspaceMapping setExternalExtension(String externalExtension) {
        this.externalExtension = externalExtension;
        return this;
    }

    public Integer getExternalMappingSource() {
        return externalMappingSource;
    }

    public WorkspaceMapping setExternalMappingSource(Integer externalMappingSource) {
        this.externalMappingSource = externalMappingSource;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkspaceMapping {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    externalMappingId: ").append(toIndentedString(externalMappingId)).append("\n");
        sb.append("    externalExtension: ").append(toIndentedString(externalExtension)).append("\n");
        sb.append("    externalMappingSource: ").append(toIndentedString(externalMappingSource)).append("\n");
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
        WorkspaceMapping workspaceMapping = (WorkspaceMapping) o;
        return Objects.equals(this.id, workspaceMapping.id) && Objects.equals(this.workspaceId,
            workspaceMapping.workspaceId) && Objects.equals(this.externalMappingId, workspaceMapping.externalMappingId)
            && Objects.equals(this.externalExtension, workspaceMapping.externalExtension) && Objects.equals(
            this.externalMappingSource, workspaceMapping.externalMappingSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workspaceId, externalMappingId, externalExtension, externalMappingSource);
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
