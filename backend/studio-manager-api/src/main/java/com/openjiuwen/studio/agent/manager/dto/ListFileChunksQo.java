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
 * ListFileChunksQo: converted from multi query params
 */
@ApiModel(description = "ListFileChunksQo: converted from multi query params")

@Validated

public class ListFileChunksQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("offset")
    @Range(min = 0L, max = 1000000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    public Integer getOffset() {
        return offset;
    }

    public ListFileChunksQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListFileChunksQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListFileChunksQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListFileChunksQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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
        ListFileChunksQo listFileChunksQo = (ListFileChunksQo) o;
        return Objects.equals(this.offset, listFileChunksQo.offset) && Objects.equals(this.limit,
            listFileChunksQo.limit) && Objects.equals(this.workspaceId, listFileChunksQo.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, workspaceId);
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
