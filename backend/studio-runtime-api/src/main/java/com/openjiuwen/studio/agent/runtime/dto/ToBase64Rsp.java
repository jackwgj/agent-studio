/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ToBase64Rsp
 */

@Validated

public class ToBase64Rsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("success")
    private Boolean success = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("base64")
    private String base64 = null;

    @JsonProperty("mimetype")
    private String mimetype = null;

    @JsonProperty("filesize")
    private Long filesize = null;

    public ToBase64Rsp setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public Boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ToBase64Rsp setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getBase64() {
        return base64;
    }

    public ToBase64Rsp setBase64(String base64) {
        this.base64 = base64;
        return this;
    }

    public String getMimetype() {
        return mimetype;
    }

    public ToBase64Rsp setMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public Long getFilesize() {
        return filesize;
    }

    public ToBase64Rsp setFilesize(Long filesize) {
        this.filesize = filesize;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ToBase64Rsp {\n");

        sb.append("    success: ").append(toIndentedString(success)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    base64: ").append(toIndentedString(base64)).append("\n");
        sb.append("    mimetype: ").append(toIndentedString(mimetype)).append("\n");
        sb.append("    filesize: ").append(toIndentedString(filesize)).append("\n");
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
        ToBase64Rsp toBase64Rsp = (ToBase64Rsp) o;
        return Objects.equals(this.success, toBase64Rsp.success) && Objects.equals(this.message, toBase64Rsp.message)
            && Objects.equals(this.base64, toBase64Rsp.base64) && Objects.equals(this.mimetype, toBase64Rsp.mimetype)
            && Objects.equals(this.filesize, toBase64Rsp.filesize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, base64, mimetype, filesize);
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
