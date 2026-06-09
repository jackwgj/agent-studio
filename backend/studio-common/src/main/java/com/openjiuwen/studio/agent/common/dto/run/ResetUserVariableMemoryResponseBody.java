/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 重置应用中用户的变量记忆结果
 */
@ApiModel(description = "重置应用中用户的变量记忆结果")
@Validated
public class ResetUserVariableMemoryResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("result")
    private Boolean result = null;

    public ResetUserVariableMemoryResponseBody setResult(Boolean result) {
        this.result = result;
        return this;
    }

    public Boolean isResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResetUserVariableMemoryResponseBody {\n");

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
        ResetUserVariableMemoryResponseBody resetUserVariableMemoryResponseBody
            = (ResetUserVariableMemoryResponseBody) o;
        return Objects.equals(this.result, resetUserVariableMemoryResponseBody.result);
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