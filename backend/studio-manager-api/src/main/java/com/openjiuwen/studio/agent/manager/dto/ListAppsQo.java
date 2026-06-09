/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ListAppsQo: converted from multi query params
 */
@ApiModel(description = "ListAppsQo: converted from multi query params")

@Validated

public class ListAppsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 5;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("tag_id")
    @Length(max = 64)
    private String tagId = null;

    @JsonProperty("source")
    private String source = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("workflow_type")
    private String workflowType = null;

    @JsonProperty("selected")
    private Boolean selected = false;

    public Integer getOffset() {
        return offset;
    }

    public ListAppsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListAppsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getName() {
        return name;
    }

    public ListAppsQo setName(String name) {
        this.name = name;
        return this;
    }

    public String getTagId() {
        return tagId;
    }

    public ListAppsQo setTagId(String tagId) {
        this.tagId = tagId;
        return this;
    }

    public String getSource() {
        return source;
    }

    public ListAppsQo setSource(String source) {
        this.source = source;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListAppsQo setType(String type) {
        this.type = type;
        return this;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public ListAppsQo setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
        return this;
    }

    public ListAppsQo setSelected(Boolean selected) {
        this.selected = selected;
        return this;
    }

    public Boolean isSelected() {
        return selected;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListAppsQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    tagId: ").append(toIndentedString(tagId)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    workflowType: ").append(toIndentedString(workflowType)).append("\n");
        sb.append("    selected: ").append(toIndentedString(selected)).append("\n");
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
        ListAppsQo listAppsQo = (ListAppsQo) o;
        return Objects.equals(this.offset, listAppsQo.offset) && Objects.equals(this.limit, listAppsQo.limit)
            && Objects.equals(this.name, listAppsQo.name) && Objects.equals(this.tagId, listAppsQo.tagId)
            && Objects.equals(this.source, listAppsQo.source) && Objects.equals(this.type, listAppsQo.type)
            && Objects.equals(this.workflowType, listAppsQo.workflowType) && Objects.equals(this.selected,
            listAppsQo.selected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, name, tagId, source, type, workflowType, selected);
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
