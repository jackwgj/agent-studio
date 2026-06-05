/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ListResourcesRelationsQo: converted from multi query params
 */
@ApiModel(description = "ListResourcesRelationsQo: converted from multi query params")

@Validated

public class ListResourcesRelationsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 0L, max = 100L)
    private Integer limit = 10;

    @JsonProperty("resource_type")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String resourceType = null;

    @JsonProperty("resource_ids")
    @Valid
    @NotNull
    @Size(min = 1, max = 64)
    private List<@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Length(min = 1, max = 64) String> resourceIds
        = new ArrayList<String>();

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    public Integer getOffset() {
        return offset;
    }

    public ListResourcesRelationsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListResourcesRelationsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public ListResourcesRelationsQo setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public ListResourcesRelationsQo setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListResourcesRelationsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListResourcesRelationsQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
        sb.append("    resourceIds: ").append(toIndentedString(resourceIds)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
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
        ListResourcesRelationsQo listResourcesRelationsQo = (ListResourcesRelationsQo) o;
        return Objects.equals(this.offset, listResourcesRelationsQo.offset) && Objects.equals(this.limit,
            listResourcesRelationsQo.limit) && Objects.equals(this.resourceType, listResourcesRelationsQo.resourceType)
            && Objects.equals(this.resourceIds, listResourcesRelationsQo.resourceIds) && Objects.equals(
            this.workspaceId, listResourcesRelationsQo.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, resourceType, resourceIds, workspaceId);
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
