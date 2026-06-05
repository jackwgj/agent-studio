/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户长期记忆列表响应体
 */
@ApiModel(description = "用户长期记忆列表响应体")

@Validated

public class ListUserLongTermMemoryResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("total")
    @NotNull
    @Range(min = 0L, max = 10000L)
    private Integer total = null;

    @JsonProperty("items")
    @Valid
    @NotNull
    @Size(max = 200)
    private List<UserLongTermMemoryInfo> items = new ArrayList<UserLongTermMemoryInfo>();

    public Integer getTotal() {
        return total;
    }

    public ListUserLongTermMemoryResponseBody setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public List<UserLongTermMemoryInfo> getItems() {
        return items;
    }

    public ListUserLongTermMemoryResponseBody setItems(List<UserLongTermMemoryInfo> items) {
        this.items = items;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListUserLongTermMemoryResponseBody {\n");

        sb.append("    total: ").append(toIndentedString(total)).append("\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
        ListUserLongTermMemoryResponseBody listUserLongTermMemoryResponseBody = (ListUserLongTermMemoryResponseBody) o;
        return Objects.equals(this.total, listUserLongTermMemoryResponseBody.total) && Objects.equals(this.items,
            listUserLongTermMemoryResponseBody.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, items);
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
