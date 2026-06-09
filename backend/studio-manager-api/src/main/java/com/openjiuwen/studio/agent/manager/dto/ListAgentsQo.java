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
 * ListAgentsQo: converted from multi query params
 */
@ApiModel(description = "ListAgentsQo: converted from multi query params")

@Validated

public class ListAgentsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 100;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(min = 1, max = 64)
    private String id = null;

    @JsonProperty("name")
    @Pattern(regexp = "^.{0,64}$")
    @Length(max = 192)
    private String name = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("description")
    @Length(min = 1, max = 256)
    private String description = null;

    @JsonProperty("creator")
    @Length(min = 1, max = 64)
    private String creator = null;

    @JsonProperty("status")
    private String status = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListAgentsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListAgentsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListAgentsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getId() {
        return id;
    }

    public ListAgentsQo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ListAgentsQo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListAgentsQo setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ListAgentsQo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public ListAgentsQo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ListAgentsQo setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListAgentsQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
        ListAgentsQo listAgentsQo = (ListAgentsQo) o;
        return Objects.equals(this.workspaceId, listAgentsQo.workspaceId) && Objects.equals(this.offset,
            listAgentsQo.offset) && Objects.equals(this.limit, listAgentsQo.limit) && Objects.equals(this.id,
            listAgentsQo.id) && Objects.equals(this.name, listAgentsQo.name) && Objects.equals(this.type,
            listAgentsQo.type) && Objects.equals(this.description, listAgentsQo.description) && Objects.equals(
            this.creator, listAgentsQo.creator) && Objects.equals(this.status, listAgentsQo.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, offset, limit, id, name, type, description, creator, status);
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
