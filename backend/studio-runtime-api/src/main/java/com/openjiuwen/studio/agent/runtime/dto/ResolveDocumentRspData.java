/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 文档解析结果
 */
@ApiModel(description = "文档解析结果")

@Validated

public class ResolveDocumentRspData implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("error_message")
    private String errorMessage = null;

    @JsonProperty("pages")
    @Valid
    @Size()
    private List<ResolvePageInfo> pages = null;

    public String getStatus() {
        return status;
    }

    public ResolveDocumentRspData setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ResolveDocumentRspData setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public List<ResolvePageInfo> getPages() {
        return pages;
    }

    public ResolveDocumentRspData setPages(List<ResolvePageInfo> pages) {
        this.pages = pages;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolveDocumentRspData {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    pages: ").append(toIndentedString(pages)).append("\n");
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
        ResolveDocumentRspData resolveDocumentRspData = (ResolveDocumentRspData) o;
        return Objects.equals(this.status, resolveDocumentRspData.status) && Objects.equals(this.errorMessage,
            resolveDocumentRspData.errorMessage) && Objects.equals(this.pages, resolveDocumentRspData.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, errorMessage, pages);
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
