/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ListLongTermMemoriesQo: converted from multi query params
 */
@ApiModel(description = "ListLongTermMemoriesQo: converted from multi query params")

@Validated

public class ListLongTermMemoriesQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_repo_id")
    @NotBlank
    @Length(min = 1, max = 64)
    private String memoryRepoId = null;

    @JsonProperty("memory_type")
    @NotBlank
    @Length(min = 1, max = 64)
    private String memoryType = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 100000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    public String getMemoryRepoId() {
        return memoryRepoId;
    }

    public ListLongTermMemoriesQo setMemoryRepoId(String memoryRepoId) {
        this.memoryRepoId = memoryRepoId;
        return this;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public ListLongTermMemoriesQo setMemoryType(String memoryType) {
        this.memoryType = memoryType;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListLongTermMemoriesQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListLongTermMemoriesQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListLongTermMemoriesQo {\n");

        sb.append("    memoryRepoId: ").append(toIndentedString(memoryRepoId)).append("\n");
        sb.append("    memoryType: ").append(toIndentedString(memoryType)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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
        ListLongTermMemoriesQo listLongTermMemoriesQo = (ListLongTermMemoriesQo) o;
        return Objects.equals(this.memoryRepoId, listLongTermMemoriesQo.memoryRepoId) && Objects.equals(this.memoryType,
            listLongTermMemoriesQo.memoryType) && Objects.equals(this.offset, listLongTermMemoriesQo.offset)
            && Objects.equals(this.limit, listLongTermMemoriesQo.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryRepoId, memoryType, offset, limit);
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
