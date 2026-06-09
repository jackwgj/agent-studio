/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * NodeRunInfoInnerError
 */
@Validated

public class NodeRunInfoInnerError implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("is_success")
    private Boolean isSuccess = null;

    @JsonProperty("error_body")
    @Valid
    private NodeRunInfoInnerErrorErrorBody errorBody = null;

    public NodeRunInfoInnerError setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
        return this;
    }

    public Boolean isIsSuccess() {
        return isSuccess;
    }

    public NodeRunInfoInnerErrorErrorBody getErrorBody() {
        return errorBody;
    }

    public NodeRunInfoInnerError setErrorBody(NodeRunInfoInnerErrorErrorBody errorBody) {
        this.errorBody = errorBody;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NodeRunInfoInnerError {\n");

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
        NodeRunInfoInnerError nodeRunInfoInnerError = (NodeRunInfoInnerError) o;
        return Objects.equals(this.isSuccess, nodeRunInfoInnerError.isSuccess) && Objects.equals(this.errorBody,
            nodeRunInfoInnerError.errorBody);
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
