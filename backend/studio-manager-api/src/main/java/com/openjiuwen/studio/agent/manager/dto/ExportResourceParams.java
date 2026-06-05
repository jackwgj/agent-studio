/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 导出相关参数。
 */
@ApiModel(description = "导出相关参数。")

@Validated

public class ExportResourceParams implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("resource_ids")
    @Valid
    @NotNull
    @Size()
    private List<@Length() String> resourceIds = new ArrayList<String>();

    @JsonProperty("resource_versions")
    @Valid
    @Size()
    private List<ExportResourceVersion> resourceVersions = null;

    @JsonProperty("resource_type")
    private String resourceType = null;

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public ExportResourceParams setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
        return this;
    }

    public List<ExportResourceVersion> getResourceVersions() {
        return resourceVersions;
    }

    public ExportResourceParams setResourceVersions(List<ExportResourceVersion> resourceVersions) {
        this.resourceVersions = resourceVersions;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public ExportResourceParams setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExportResourceParams {\n");

        sb.append("    resourceIds: ").append(toIndentedString(resourceIds)).append("\n");
        sb.append("    resourceVersions: ").append(toIndentedString(resourceVersions)).append("\n");
        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
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
        ExportResourceParams exportResourceParams = (ExportResourceParams) o;
        return Objects.equals(this.resourceIds, exportResourceParams.resourceIds) && Objects.equals(
            this.resourceVersions, exportResourceParams.resourceVersions) && Objects.equals(this.resourceType,
            exportResourceParams.resourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceIds, resourceVersions, resourceType);
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
