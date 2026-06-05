/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ResolveDocumentReq
 */

@Validated

public class ResolveDocumentReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("urls")
    @Valid
    @NotNull
    @Size()
    private List<@Length() String> urls = new ArrayList<String>();

    @JsonProperty("ocr_enabled")
    private Boolean ocrEnabled = true;

    public List<@Size(min = 1, max = 2550) String> getUrls() {
        return urls;
    }

    public ResolveDocumentReq setUrls(List<@Size(min = 1, max = 2550) String> urls) {
        this.urls = urls;
        return this;
    }

    public ResolveDocumentReq setOcrEnabled(Boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
        return this;
    }

    public Boolean isOcrEnabled() {
        return ocrEnabled;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolveDocumentReq {\n");

        sb.append("    urls: ").append(toIndentedString(urls)).append("\n");
        sb.append("    ocrEnabled: ").append(toIndentedString(ocrEnabled)).append("\n");
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
        ResolveDocumentReq resolveDocumentReq = (ResolveDocumentReq) o;
        return Objects.equals(this.urls, resolveDocumentReq.urls) && Objects.equals(this.ocrEnabled,
            resolveDocumentReq.ocrEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urls, ocrEnabled);
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
