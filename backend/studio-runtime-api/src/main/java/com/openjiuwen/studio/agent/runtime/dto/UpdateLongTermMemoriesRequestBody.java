/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 修改用户画像记忆值的请求消息体
 */
@ApiModel(description = "修改用户画像记忆值的请求消息体")

@Validated

public class UpdateLongTermMemoriesRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memories")
    @Valid
    @Size()
    private List<MemoryUpdate> memories = null;

    public List<MemoryUpdate> getMemories() {
        return memories;
    }

    public UpdateLongTermMemoriesRequestBody setMemories(List<MemoryUpdate> memories) {
        this.memories = memories;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateLongTermMemoriesRequestBody {\n");

        sb.append("    memories: ").append(toIndentedString(memories)).append("\n");
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
        UpdateLongTermMemoriesRequestBody updateLongTermMemoriesRequestBody = (UpdateLongTermMemoriesRequestBody) o;
        return Objects.equals(this.memories, updateLongTermMemoriesRequestBody.memories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memories);
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
