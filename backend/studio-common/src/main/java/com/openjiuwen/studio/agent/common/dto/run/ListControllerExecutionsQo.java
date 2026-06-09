/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ListControllerExecutionsQo: converted from multi query params
 */
@ApiModel(description = "ListControllerExecutionsQo: converted from multi query params")
@Validated
public class ListControllerExecutionsQo {
    @JsonProperty("offset")
    @Range(min = 0L, max = 100000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    public Integer getOffset() {
        return offset;
    }

    public ListControllerExecutionsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListControllerExecutionsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListControllerExecutionsQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
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
        ListControllerExecutionsQo listControllerExecutionsQo = (ListControllerExecutionsQo) o;
        return Objects.equals(this.offset, listControllerExecutionsQo.offset) && Objects.equals(this.limit,
            listControllerExecutionsQo.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit);
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
