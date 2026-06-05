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
 * ListDatasourceQo: converted from multi query params
 */
@ApiModel(description = "ListDatasourceQo: converted from multi query params")

@Validated

public class ListDatasourceQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 100000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 1000;

    @JsonProperty("sort")
    @Length(max = 5)
    private String sort = "desc";

    @JsonProperty("name")
    @Pattern(regexp = "^.{0,64}$")
    @Length(max = 192)
    private String name = null;

    @JsonProperty("type")
    @Length(max = 16)
    private String type = null;

    @JsonProperty("internet_access")
    @Length(max = 16)
    private String internetAccess = null;

    @JsonProperty("status")
    @Length(max = 32)
    private String status = null;

    @JsonProperty("creator")
    @Length(max = 32)
    private String creator = null;

    @JsonProperty("updater")
    @Length(max = 32)
    private String updater = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListDatasourceQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListDatasourceQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListDatasourceQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public ListDatasourceQo setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public String getName() {
        return name;
    }

    public ListDatasourceQo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListDatasourceQo setType(String type) {
        this.type = type;
        return this;
    }

    public String getInternetAccess() {
        return internetAccess;
    }

    public ListDatasourceQo setInternetAccess(String internetAccess) {
        this.internetAccess = internetAccess;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ListDatasourceQo setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public ListDatasourceQo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public ListDatasourceQo setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListDatasourceQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    internetAccess: ").append(toIndentedString(internetAccess)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    updater: ").append(toIndentedString(updater)).append("\n");
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
        ListDatasourceQo listDatasourceQo = (ListDatasourceQo) o;
        return Objects.equals(this.workspaceId, listDatasourceQo.workspaceId) && Objects.equals(this.offset,
            listDatasourceQo.offset) && Objects.equals(this.limit, listDatasourceQo.limit) && Objects.equals(this.sort,
            listDatasourceQo.sort) && Objects.equals(this.name, listDatasourceQo.name) && Objects.equals(this.type,
            listDatasourceQo.type) && Objects.equals(this.internetAccess, listDatasourceQo.internetAccess)
            && Objects.equals(this.status, listDatasourceQo.status) && Objects.equals(this.creator,
            listDatasourceQo.creator) && Objects.equals(this.updater, listDatasourceQo.updater);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, offset, limit, sort, name, type, internetAccess, status, creator, updater);
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
