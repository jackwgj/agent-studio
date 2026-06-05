/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 数据源执行请求
 */
@ApiModel(description = "数据源执行请求")

@Validated

public class DatasourceExecuteReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("query")
    @NotBlank
    private String query = null;

    @JsonProperty("should_by_cache")
    private Boolean shouldByCache = true;

    @JsonProperty("conditions")
    @Valid
    @Size()
    private List<@Length() String> conditions = null;

    public String getQuery() {
        return query;
    }

    public DatasourceExecuteReq setQuery(String query) {
        this.query = query;
        return this;
    }

    public DatasourceExecuteReq setShouldByCache(Boolean shouldByCache) {
        this.shouldByCache = shouldByCache;
        return this;
    }

    public Boolean isShouldByCache() {
        return shouldByCache;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public DatasourceExecuteReq setConditions(List<String> conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceExecuteReq {\n");

        sb.append("    query: ").append(toIndentedString(query)).append("\n");
        sb.append("    shouldByCache: ").append(toIndentedString(shouldByCache)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
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
        DatasourceExecuteReq datasourceExecuteReq = (DatasourceExecuteReq) o;
        return Objects.equals(this.query, datasourceExecuteReq.query) && Objects.equals(this.shouldByCache,
            datasourceExecuteReq.shouldByCache) && Objects.equals(this.conditions, datasourceExecuteReq.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, shouldByCache, conditions);
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
