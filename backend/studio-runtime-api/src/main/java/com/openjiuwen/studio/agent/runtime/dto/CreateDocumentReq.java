/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * CreateDocumentReq
 */

@Validated

public class CreateDocumentReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("input")
    @NotBlank
    private String input = null;

    @JsonProperty("document_name")
    @NotBlank
    private String documentName = null;

    @JsonProperty("expires")
    private Integer expires = null;

    public String getInput() {
        return input;
    }

    public CreateDocumentReq setInput(String input) {
        this.input = input;
        return this;
    }

    public String getDocumentName() {
        return documentName;
    }

    public CreateDocumentReq setDocumentName(String documentName) {
        this.documentName = documentName;
        return this;
    }

    public Integer getExpires() {
        return expires;
    }

    public CreateDocumentReq setExpires(Integer expires) {
        this.expires = expires;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreateDocumentReq {\n");

        sb.append("    input: ").append(toIndentedString(input)).append("\n");
        sb.append("    documentName: ").append(toIndentedString(documentName)).append("\n");
        sb.append("    expires: ").append(toIndentedString(expires)).append("\n");
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
        CreateDocumentReq createDocumentReq = (CreateDocumentReq) o;
        return Objects.equals(this.input, createDocumentReq.input) && Objects.equals(this.documentName,
            createDocumentReq.documentName) && Objects.equals(this.expires, createDocumentReq.expires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, documentName, expires);
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
