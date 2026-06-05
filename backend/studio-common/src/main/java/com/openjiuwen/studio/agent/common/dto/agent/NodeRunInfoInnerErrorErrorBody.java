/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * NodeRunInfoInnerErrorErrorBody
 */
@Validated
public class NodeRunInfoInnerErrorErrorBody {
    @JsonProperty("error_code")
    private String errorCode = null;

    @JsonProperty("error_message")
    private String errorMessage = null;

    public String getErrorCode() {
        return errorCode;
    }

    public NodeRunInfoInnerErrorErrorBody setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public NodeRunInfoInnerErrorErrorBody setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NodeRunInfoInnerErrorErrorBody {\n");

        sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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
        NodeRunInfoInnerErrorErrorBody nodeRunInfoInnerErrorErrorBody = (NodeRunInfoInnerErrorErrorBody) o;
        return Objects.equals(this.errorCode, nodeRunInfoInnerErrorErrorBody.errorCode) && Objects.equals(
            this.errorMessage, nodeRunInfoInnerErrorErrorBody.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, errorMessage);
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
