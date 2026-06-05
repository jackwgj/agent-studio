/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 调测接口错误信息
 */
@ApiModel(description = "调测接口错误信息")

@Validated

public class JiuwenEventDataError implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("error_code")
    private Integer errorCode = null;

    @JsonProperty("message")
    private String message = null;

    public Integer getErrorCode() {
        return errorCode;
    }

    public JiuwenEventDataError setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public JiuwenEventDataError setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenEventDataError {\n");

        sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
        JiuwenEventDataError jiuwenEventDataError = (JiuwenEventDataError) o;
        return Objects.equals(this.errorCode, jiuwenEventDataError.errorCode) && Objects.equals(this.message,
            jiuwenEventDataError.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, message);
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
