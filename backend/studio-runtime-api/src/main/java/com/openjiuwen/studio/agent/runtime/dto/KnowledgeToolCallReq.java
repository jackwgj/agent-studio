/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * KnowledgeToolCallReq
 */

@Validated

public class KnowledgeToolCallReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("query")
    @NotBlank
    @Length(max = 10000)
    private String query = null;

    @JsonProperty("filter_string")
    @Length(max = 1024)
    private String filterString = null;

    @JsonProperty("scope")
    @Length(max = 16)
    private String scope = null;

    public String getQuery() {
        return query;
    }

    public KnowledgeToolCallReq setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getFilterString() {
        return filterString;
    }

    public KnowledgeToolCallReq setFilterString(String filterString) {
        this.filterString = filterString;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public KnowledgeToolCallReq setScope(String scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KnowledgeToolCallReq {\n");

        sb.append("    query: ").append(toIndentedString(query)).append("\n");
        sb.append("    filterString: ").append(toIndentedString(filterString)).append("\n");
        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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
        KnowledgeToolCallReq knowledgeToolCallReq = (KnowledgeToolCallReq) o;
        return Objects.equals(this.query, knowledgeToolCallReq.query) && Objects.equals(this.filterString,
            knowledgeToolCallReq.filterString) && Objects.equals(this.scope, knowledgeToolCallReq.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, filterString, scope);
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
