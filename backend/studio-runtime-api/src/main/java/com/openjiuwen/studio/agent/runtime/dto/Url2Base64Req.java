/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * Url2Base64Req
 */

@Validated

public class Url2Base64Req implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("url")
    @NotBlank
    private String url = null;

    @JsonProperty("isDataUrl")
    @NotNull
    private Boolean isDataUrl = null;

    public String getUrl() {
        return url;
    }

    public Url2Base64Req setUrl(String url) {
        this.url = url;
        return this;
    }

    public Url2Base64Req setIsDataUrl(Boolean isDataUrl) {
        this.isDataUrl = isDataUrl;
        return this;
    }

    public Boolean isIsDataUrl() {
        return isDataUrl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Url2Base64Req {\n");

        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    isDataUrl: ").append(toIndentedString(isDataUrl)).append("\n");
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
        Url2Base64Req url2Base64Req = (Url2Base64Req) o;
        return Objects.equals(this.url, url2Base64Req.url) && Objects.equals(this.isDataUrl, url2Base64Req.isDataUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, isDataUrl);
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
