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
 * Base64ToImageReq
 */

@Validated

public class Base64ToImageReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("base64")
    @NotBlank
    private String base64 = null;

    @JsonProperty("mimetype")
    @NotBlank
    private String mimetype = null;

    public String getBase64() {
        return base64;
    }

    public Base64ToImageReq setBase64(String base64) {
        this.base64 = base64;
        return this;
    }

    public String getMimetype() {
        return mimetype;
    }

    public Base64ToImageReq setMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Base64ToImageReq {\n");

        sb.append("    base64: ").append(toIndentedString(base64)).append("\n");
        sb.append("    mimetype: ").append(toIndentedString(mimetype)).append("\n");
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
        Base64ToImageReq base64ToImageReq = (Base64ToImageReq) o;
        return Objects.equals(this.base64, base64ToImageReq.base64) && Objects.equals(this.mimetype,
            base64ToImageReq.mimetype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base64, mimetype);
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
