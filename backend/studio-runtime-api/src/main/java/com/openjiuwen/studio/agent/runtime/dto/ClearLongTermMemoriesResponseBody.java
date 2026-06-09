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
 * 清空记忆内容的响应消息体
 */
@ApiModel(description = "清空记忆内容的响应消息体")

@Validated

public class ClearLongTermMemoriesResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("success")
    private Boolean success = null;

    public ClearLongTermMemoriesResponseBody setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public Boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClearLongTermMemoriesResponseBody {\n");

        sb.append("    success: ").append(toIndentedString(success)).append("\n");
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
        ClearLongTermMemoriesResponseBody clearLongTermMemoriesResponseBody = (ClearLongTermMemoriesResponseBody) o;
        return Objects.equals(this.success, clearLongTermMemoriesResponseBody.success);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success);
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
