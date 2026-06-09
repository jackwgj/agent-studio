/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ListConversationQueriesQo: converted from multi query params
 */
@ApiModel(description = "ListConversationQueriesQo: converted from multi query params")
@Validated
public class ListConversationQueriesQo {
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

    @JsonProperty("version")
    @Length(max = 64)
    private String version = null;

    public Integer getOffset() {
        return offset;
    }

    public ListConversationQueriesQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListConversationQueriesQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ListConversationQueriesQo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public ListConversationQueriesQo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ListConversationQueriesQo setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListConversationQueriesQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
        ListConversationQueriesQo listConversationQueriesQo = (ListConversationQueriesQo) o;
        return Objects.equals(this.offset, listConversationQueriesQo.offset) && Objects.equals(this.limit,
            listConversationQueriesQo.limit) && Objects.equals(this.startTime, listConversationQueriesQo.startTime)
            && Objects.equals(this.endTime, listConversationQueriesQo.endTime) && Objects.equals(this.version,
            listConversationQueriesQo.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, startTime, endTime, version);
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
