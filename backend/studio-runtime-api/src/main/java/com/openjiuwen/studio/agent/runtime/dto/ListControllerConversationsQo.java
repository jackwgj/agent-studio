/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ListControllerConversationsQo: converted from multi query params
 */
@ApiModel(description = "ListControllerConversationsQo: converted from multi query params")

@Validated

public class ListControllerConversationsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("offset")
    @Range(min = 0L, max = 100000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    @JsonProperty("start_time")
    private Long startTime = null;

    @JsonProperty("end_time")
    private Long endTime = null;

    public Integer getOffset() {
        return offset;
    }

    public ListControllerConversationsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListControllerConversationsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ListControllerConversationsQo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public ListControllerConversationsQo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListControllerConversationsQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
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
        ListControllerConversationsQo listControllerConversationsQo = (ListControllerConversationsQo) o;
        return Objects.equals(this.offset, listControllerConversationsQo.offset) && Objects.equals(this.limit,
            listControllerConversationsQo.limit) && Objects.equals(this.startTime,
            listControllerConversationsQo.startTime) && Objects.equals(this.endTime,
            listControllerConversationsQo.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, startTime, endTime);
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
