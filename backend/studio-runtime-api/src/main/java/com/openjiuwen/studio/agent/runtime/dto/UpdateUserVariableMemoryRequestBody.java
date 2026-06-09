/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 修改用户变量记忆的请求体
 */
@ApiModel(description = "修改用户变量记忆的请求体")

@Validated

public class UpdateUserVariableMemoryRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("variable_value")
    @Length(max = 500)
    private String variableValue = null;

    public String getVariableValue() {
        return variableValue;
    }

    public UpdateUserVariableMemoryRequestBody setVariableValue(String variableValue) {
        this.variableValue = variableValue;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateUserVariableMemoryRequestBody {\n");

        sb.append("    variableValue: ").append(toIndentedString(variableValue)).append("\n");
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
        UpdateUserVariableMemoryRequestBody updateUserVariableMemoryRequestBody
            = (UpdateUserVariableMemoryRequestBody) o;
        return Objects.equals(this.variableValue, updateUserVariableMemoryRequestBody.variableValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableValue);
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
