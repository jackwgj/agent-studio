/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

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
 * ListWorkflowsQo: converted from multi query params
 */
@ApiModel(description = "ListWorkflowsQo: converted from multi query params")

@Validated

public class ListWorkflowsQo implements Serializable {
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
    @Length(max = 128)
    private String id = null;

    @JsonProperty("name")
    @Pattern(regexp = "^.{0,64}$")
    @Length(max = 192)
    private String name = null;

    @JsonProperty("code")
    @Length(max = 128)
    private String code = null;

    @JsonProperty("description")
    @Length(max = 512)
    private String description = null;

    @JsonProperty("creator")
    @Length(max = 128)
    private String creator = null;

    @JsonProperty("group")
    @Length(max = 128)
    private String group = null;

    @JsonProperty("type")
    @Length(max = 128)
    private String type = null;

    @JsonProperty("customize_node")
    private Boolean customizeNode = null;

    @JsonProperty("status")
    private String status = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListWorkflowsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListWorkflowsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListWorkflowsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getId() {
        return id;
    }

    public ListWorkflowsQo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ListWorkflowsQo setName(String name) {
        this.name = name;
        return this;
    }

    public String getCode() {
        return code;
    }

    public ListWorkflowsQo setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ListWorkflowsQo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public ListWorkflowsQo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ListWorkflowsQo setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListWorkflowsQo setType(String type) {
        this.type = type;
        return this;
    }

    public ListWorkflowsQo setCustomizeNode(Boolean customizeNode) {
        this.customizeNode = customizeNode;
        return this;
    }

    public Boolean isCustomizeNode() {
        return customizeNode;
    }

    public String getStatus() {
        return status;
    }

    public ListWorkflowsQo setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListWorkflowsQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    customizeNode: ").append(toIndentedString(customizeNode)).append("\n");
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
        ListWorkflowsQo listWorkflowsQo = (ListWorkflowsQo) o;
        return Objects.equals(this.workspaceId, listWorkflowsQo.workspaceId) && Objects.equals(this.offset,
            listWorkflowsQo.offset) && Objects.equals(this.limit, listWorkflowsQo.limit) && Objects.equals(this.id,
            listWorkflowsQo.id) && Objects.equals(this.name, listWorkflowsQo.name) && Objects.equals(this.code,
            listWorkflowsQo.code) && Objects.equals(this.description, listWorkflowsQo.description) && Objects.equals(
            this.creator, listWorkflowsQo.creator) && Objects.equals(this.group, listWorkflowsQo.group)
            && Objects.equals(this.type, listWorkflowsQo.type) && Objects.equals(this.customizeNode,
            listWorkflowsQo.customizeNode) && Objects.equals(this.status, listWorkflowsQo.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, offset, limit, id, name, code, description, creator, group, type,
            customizeNode, status);
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
