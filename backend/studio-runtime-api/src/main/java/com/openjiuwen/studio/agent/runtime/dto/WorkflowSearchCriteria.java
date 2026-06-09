/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * workflow搜索条件
 */
@ApiModel(description = "workflow搜索条件")

@Validated

public class WorkflowSearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("author")
    private String author = null;

    @JsonProperty("group")
    private String group = null;

    @JsonProperty("workflow_type")
    private String workflowType = null;

    public String getId() {
        return id;
    }

    public WorkflowSearchCriteria setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkflowSearchCriteria setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowSearchCriteria setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public WorkflowSearchCriteria setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public WorkflowSearchCriteria setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public WorkflowSearchCriteria setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowSearchCriteria {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    author: ").append(toIndentedString(author)).append("\n");
        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("    workflowType: ").append(toIndentedString(workflowType)).append("\n");
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
        WorkflowSearchCriteria workflowSearchCriteria = (WorkflowSearchCriteria) o;
        return Objects.equals(this.id, workflowSearchCriteria.id) && Objects.equals(this.name,
            workflowSearchCriteria.name) && Objects.equals(this.description, workflowSearchCriteria.description)
            && Objects.equals(this.author, workflowSearchCriteria.author) && Objects.equals(this.group,
            workflowSearchCriteria.group) && Objects.equals(this.workflowType, workflowSearchCriteria.workflowType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, author, group, workflowType);
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
