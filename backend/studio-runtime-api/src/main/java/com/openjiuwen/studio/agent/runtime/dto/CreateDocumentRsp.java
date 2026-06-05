/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * CreateDocumentRsp
 */

@Validated

public class CreateDocumentRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("url")
    private String url = null;

    public String getUrl() {
        return url;
    }

    public CreateDocumentRsp setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreateDocumentRsp {\n");

        sb.append("    url: ").append(toIndentedString(url)).append("\n");
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
        CreateDocumentRsp createDocumentRsp = (CreateDocumentRsp) o;
        return Objects.equals(this.url, createDocumentRsp.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
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
