/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ListAgentConversationsQo: converted from multi query params
 */
@ApiModel(description = "ListAgentConversationsQo: converted from multi query params")
@Validated
public class ListAgentConversationsQo {
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

    @JsonProperty("type")
    @Length(max = 32)
    private String type = "agent";

    public Integer getOffset() {
        return offset;
    }

    public ListAgentConversationsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListAgentConversationsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ListAgentConversationsQo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public ListAgentConversationsQo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListAgentConversationsQo setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListAgentConversationsQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
        ListAgentConversationsQo listAgentConversationsQo = (ListAgentConversationsQo) o;
        return Objects.equals(this.offset, listAgentConversationsQo.offset) && Objects.equals(this.limit,
            listAgentConversationsQo.limit) && Objects.equals(this.startTime, listAgentConversationsQo.startTime)
            && Objects.equals(this.endTime, listAgentConversationsQo.endTime) && Objects.equals(this.type,
            listAgentConversationsQo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, startTime, endTime, type);
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
