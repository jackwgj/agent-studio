/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ResolvePageInfo
 */

@Validated

public class ResolvePageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("content")
    @Valid
    @Size()
    private List<@Length() String> content = null;

    @JsonProperty("page_num")
    private Integer pageNum = null;

    public List<String> getContent() {
        return content;
    }

    public ResolvePageInfo setContent(List<String> content) {
        this.content = content;
        return this;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public ResolvePageInfo setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolvePageInfo {\n");

        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    pageNum: ").append(toIndentedString(pageNum)).append("\n");
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
        ResolvePageInfo resolvePageInfo = (ResolvePageInfo) o;
        return Objects.equals(this.content, resolvePageInfo.content) && Objects.equals(this.pageNum,
            resolvePageInfo.pageNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, pageNum);
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
