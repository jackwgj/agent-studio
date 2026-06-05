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
 * ListAgentLastVersionsQo: converted from multi query params
 */
@ApiModel(description = "ListAgentLastVersionsQo: converted from multi query params")

@Validated

public class ListAgentLastVersionsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 65535L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 10000L)
    private Integer limit = 5;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(min = 1, max = 64)
    private String id = null;

    @JsonProperty("name")
    @Pattern(regexp = "^.{0,64}$")
    @Length(max = 192)
    private String name = null;

    @JsonProperty("type")
    @Length(max = 16)
    private String type = null;

    @JsonProperty("sub_type")
    @Length(max = 32)
    private String subType = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListAgentLastVersionsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListAgentLastVersionsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListAgentLastVersionsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getId() {
        return id;
    }

    public ListAgentLastVersionsQo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ListAgentLastVersionsQo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListAgentLastVersionsQo setType(String type) {
        this.type = type;
        return this;
    }

    public String getSubType() {
        return subType;
    }

    public ListAgentLastVersionsQo setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListAgentLastVersionsQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    subType: ").append(toIndentedString(subType)).append("\n");
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
        ListAgentLastVersionsQo listAgentLastVersionsQo = (ListAgentLastVersionsQo) o;
        return Objects.equals(this.workspaceId, listAgentLastVersionsQo.workspaceId) && Objects.equals(this.offset,
            listAgentLastVersionsQo.offset) && Objects.equals(this.limit, listAgentLastVersionsQo.limit)
            && Objects.equals(this.id, listAgentLastVersionsQo.id) && Objects.equals(this.name,
            listAgentLastVersionsQo.name) && Objects.equals(this.type, listAgentLastVersionsQo.type) && Objects.equals(
            this.subType, listAgentLastVersionsQo.subType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, offset, limit, id, name, type, subType);
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
