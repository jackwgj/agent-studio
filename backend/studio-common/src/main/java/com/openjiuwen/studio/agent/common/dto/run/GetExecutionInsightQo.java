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
 * GetExecutionInsightQo: converted from multi query params
 */
@ApiModel(description = "GetExecutionInsightQo: converted from multi query params")
@Validated
public class GetExecutionInsightQo {
    @JsonProperty("offset")
    @Range(min = 0L, max = 100000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    @JsonProperty("version")
    @Length(max = 64)
    private String version = null;

    public Integer getOffset() {
        return offset;
    }

    public GetExecutionInsightQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public GetExecutionInsightQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public GetExecutionInsightQo setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GetExecutionInsightQo {\n");

        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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
        GetExecutionInsightQo getExecutionInsightQo = (GetExecutionInsightQo) o;
        return Objects.equals(this.offset, getExecutionInsightQo.offset) && Objects.equals(this.limit,
            getExecutionInsightQo.limit) && Objects.equals(this.version, getExecutionInsightQo.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, version);
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
