/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * JiuwenEventDataInnerError
 */

@Validated

public class JiuwenEventDataInnerError implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("isSuccess")
    private Boolean isSuccess = null;

    @JsonProperty("errorBody")
    @Valid
    private JiuwenEventDataInnerErrorErrorBody errorBody = null;

    public JiuwenEventDataInnerError setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
        return this;
    }

    public Boolean isIsSuccess() {
        return isSuccess;
    }

    public JiuwenEventDataInnerErrorErrorBody getErrorBody() {
        return errorBody;
    }

    public JiuwenEventDataInnerError setErrorBody(JiuwenEventDataInnerErrorErrorBody errorBody) {
        this.errorBody = errorBody;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenEventDataInnerError {\n");

        sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
        sb.append("    errorBody: ").append(toIndentedString(errorBody)).append("\n");
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
        JiuwenEventDataInnerError jiuwenEventDataInnerError = (JiuwenEventDataInnerError) o;
        return Objects.equals(this.isSuccess, jiuwenEventDataInnerError.isSuccess) && Objects.equals(this.errorBody,
            jiuwenEventDataInnerError.errorBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuccess, errorBody);
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
