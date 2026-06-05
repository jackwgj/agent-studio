/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ListResourceRelationsQo: converted from multi query params
 */
@ApiModel(description = "ListResourceRelationsQo: converted from multi query params")

@Validated

public class ListResourceRelationsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("resource_type")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String resourceType = null;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 0L, max = 100L)
    private Integer limit = 10;

    @JsonProperty("version")
    @Length(max = 64)
    private String version = null;

    @JsonProperty("app_type")
    @Length(max = 64)
    private String appType = null;

    @JsonProperty("reference_type")
    private String referenceType = "all";

    public String getResourceType() {
        return resourceType;
    }

    public ListResourceRelationsQo setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListResourceRelationsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListResourceRelationsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListResourceRelationsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ListResourceRelationsQo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getAppType() {
        return appType;
    }

    public ListResourceRelationsQo setAppType(String appType) {
        this.appType = appType;
        return this;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public ListResourceRelationsQo setReferenceType(String referenceType) {
        this.referenceType = referenceType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListResourceRelationsQo {\n");

        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    appType: ").append(toIndentedString(appType)).append("\n");
        sb.append("    referenceType: ").append(toIndentedString(referenceType)).append("\n");
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
        ListResourceRelationsQo listResourceRelationsQo = (ListResourceRelationsQo) o;
        return Objects.equals(this.resourceType, listResourceRelationsQo.resourceType) && Objects.equals(
            this.workspaceId, listResourceRelationsQo.workspaceId) && Objects.equals(this.offset,
            listResourceRelationsQo.offset) && Objects.equals(this.limit, listResourceRelationsQo.limit)
            && Objects.equals(this.version, listResourceRelationsQo.version) && Objects.equals(this.appType,
            listResourceRelationsQo.appType) && Objects.equals(this.referenceType,
            listResourceRelationsQo.referenceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, workspaceId, offset, limit, version, appType, referenceType);
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
