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
 * 用户画像记忆值的响应消息体
 */
@ApiModel(description = "用户画像记忆值的响应消息体")

@Validated

public class ListLongTermMemoriesResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("total")
    private Integer total = null;

    @JsonProperty("memories")
    @Valid
    @Size()
    private List<Memory> memories = null;

    public Integer getTotal() {
        return total;
    }

    public ListLongTermMemoriesResponseBody setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public List<Memory> getMemories() {
        return memories;
    }

    public ListLongTermMemoriesResponseBody setMemories(List<Memory> memories) {
        this.memories = memories;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListLongTermMemoriesResponseBody {\n");

        sb.append("    total: ").append(toIndentedString(total)).append("\n");
        sb.append("    memories: ").append(toIndentedString(memories)).append("\n");
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
        ListLongTermMemoriesResponseBody listLongTermMemoriesResponseBody = (ListLongTermMemoriesResponseBody) o;
        return Objects.equals(this.total, listLongTermMemoriesResponseBody.total) && Objects.equals(this.memories,
            listLongTermMemoriesResponseBody.memories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, memories);
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
