/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 更新变量详情
 */
@ApiModel(description = "更新变量详情")

@Validated

public class UpdateVariableReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("value")
    @Valid
    private Object value = null;

    public String getName() {
        return name;
    }

    public UpdateVariableReq setName(String name) {
        this.name = name;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public UpdateVariableReq setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateVariableReq {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
        UpdateVariableReq updateVariableReq = (UpdateVariableReq) o;
        return Objects.equals(this.name, updateVariableReq.name) && Objects.equals(this.value, updateVariableReq.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
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
