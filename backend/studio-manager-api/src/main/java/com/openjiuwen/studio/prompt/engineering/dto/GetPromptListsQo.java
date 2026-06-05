/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * GetPromptListsQo: converted from multi query params
 */
@ApiModel(description = "GetPromptListsQo: converted from multi query params")

@Validated
public class GetPromptListsQo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @NotBlank
    private String workspaceId = null;

    @JsonProperty("limit")
    @NotNull
    @Range(min = 10L, max = 1000L)
    private Integer limit = 10;

    @JsonProperty("offset")
    @NotNull
    @Range(min = 0L, max = 10000L)
    private Integer offset = null;

    @JsonProperty("name")
    @Length(max = 255)
    private String name = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public GetPromptListsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public GetPromptListsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public GetPromptListsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public String getName() {
        return name;
    }

    public GetPromptListsQo setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GetPromptListsQo {\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
        GetPromptListsQo getPromptListsQo = (GetPromptListsQo) o;
        return Objects.equals(this.workspaceId, getPromptListsQo.workspaceId) && Objects.equals(this.limit,
            getPromptListsQo.limit) && Objects.equals(this.offset, getPromptListsQo.offset) && Objects.equals(this.name,
            getPromptListsQo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, limit, offset, name);
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
