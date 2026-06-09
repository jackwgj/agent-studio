/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * PromptBuilder流式响应体
 */
@ApiModel(description = "PromptBuilder流式响应体")

@Validated

public class PromptBuilderRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("code")
    private Integer code = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("data")
    private String data = null;

    public Integer getCode() {
        return code;
    }

    public PromptBuilderRsp setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public PromptBuilderRsp setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getData() {
        return data;
    }

    public PromptBuilderRsp setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PromptBuilderRsp {\n");

        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
        PromptBuilderRsp promptBuilderRsp = (PromptBuilderRsp) o;
        return Objects.equals(this.code, promptBuilderRsp.code) && Objects.equals(this.message,
            promptBuilderRsp.message) && Objects.equals(this.data, promptBuilderRsp.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
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
