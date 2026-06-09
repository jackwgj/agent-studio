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
 * GetMemoryChunksQo: converted from multi query params
 */
@ApiModel(description = "GetMemoryChunksQo: converted from multi query params")

@Validated

public class GetMemoryChunksQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("limit")
    private Integer limit = null;

    public Integer getLimit() {
        return limit;
    }

    public GetMemoryChunksQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GetMemoryChunksQo {\n");

        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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
        GetMemoryChunksQo getMemoryChunksQo = (GetMemoryChunksQo) o;
        return Objects.equals(this.limit, getMemoryChunksQo.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit);
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
