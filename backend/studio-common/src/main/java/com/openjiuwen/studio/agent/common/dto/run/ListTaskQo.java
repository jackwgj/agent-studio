/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ListTaskQo: converted from multi query params
 */
@ApiModel
@Validated
public class ListTaskQo {
    @JsonProperty("status")
    @Length(max = 64)
    private String status = null;

    @JsonProperty("type")
    @Length(max = 64)
    private String type = null;

    @JsonProperty("sort")
    private String sort = "create_time";

    @JsonProperty("order")
    private String order = "desc";

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    public String getStatus() {
        return status;
    }

    public ListTaskQo setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListTaskQo setType(String type) {
        this.type = type;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public ListTaskQo setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public ListTaskQo setOrder(String order) {
        this.order = order;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListTaskQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListTaskQo {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
        sb.append("    order: ").append(toIndentedString(order)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
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
        ListTaskQo listTaskQo = (ListTaskQo) o;
        return Objects.equals(this.status, listTaskQo.status) && Objects.equals(this.type, listTaskQo.type)
            && Objects.equals(this.sort, listTaskQo.sort) && Objects.equals(this.order, listTaskQo.order)
            && Objects.equals(this.workspaceId, listTaskQo.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, type, sort, order, workspaceId);
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
