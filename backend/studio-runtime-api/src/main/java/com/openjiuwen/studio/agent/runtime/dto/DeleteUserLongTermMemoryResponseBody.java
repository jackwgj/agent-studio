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
 * 删除用户长期记忆的响应体
 */
@ApiModel(description = "删除用户长期记忆的响应体")

@Validated

public class DeleteUserLongTermMemoryResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("result")
    private Boolean result = null;

    public DeleteUserLongTermMemoryResponseBody setResult(Boolean result) {
        this.result = result;
        return this;
    }

    public Boolean isResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeleteUserLongTermMemoryResponseBody {\n");

        sb.append("    result: ").append(toIndentedString(result)).append("\n");
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
        DeleteUserLongTermMemoryResponseBody deleteUserLongTermMemoryResponseBody
            = (DeleteUserLongTermMemoryResponseBody) o;
        return Objects.equals(this.result, deleteUserLongTermMemoryResponseBody.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
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
