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
 * 删除记忆内容的响应消息体
 */
@ApiModel(description = "删除记忆内容的响应消息体")

@Validated

public class DeleteLongTermMemoriesResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("result")
    private Boolean result = null;

    public DeleteLongTermMemoriesResponseBody setResult(Boolean result) {
        this.result = result;
        return this;
    }

    public Boolean isResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeleteLongTermMemoriesResponseBody {\n");

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
        DeleteLongTermMemoriesResponseBody deleteLongTermMemoriesResponseBody = (DeleteLongTermMemoriesResponseBody) o;
        return Objects.equals(this.result, deleteLongTermMemoriesResponseBody.result);
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
