/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 删除用户画像记忆值的响应消息体
 */
@ApiModel(description = "删除用户画像记忆值的响应消息体")

@Validated

public class DeleteLongTermMemoriesRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_ids")
    @Valid
    @Size()
    private List<@Length() String> memoryIds = null;

    public List<String> getMemoryIds() {
        return memoryIds;
    }

    public DeleteLongTermMemoriesRequestBody setMemoryIds(List<String> memoryIds) {
        this.memoryIds = memoryIds;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeleteLongTermMemoriesRequestBody {\n");

        sb.append("    memoryIds: ").append(toIndentedString(memoryIds)).append("\n");
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
        DeleteLongTermMemoriesRequestBody deleteLongTermMemoriesRequestBody = (DeleteLongTermMemoriesRequestBody) o;
        return Objects.equals(this.memoryIds, deleteLongTermMemoriesRequestBody.memoryIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryIds);
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
