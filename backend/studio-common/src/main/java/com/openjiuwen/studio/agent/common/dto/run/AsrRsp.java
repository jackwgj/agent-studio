/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * 接口响应体。
 */
@ApiModel(description = "接口响应体。")
@Validated
public class AsrRsp {
    @JsonProperty("trace_id")
    @NotBlank
    private String traceId = null;

    @JsonProperty("result")
    @Valid
    @NotNull
    private Result result = null;

    public String getTraceId() {
        return traceId;
    }

    public AsrRsp setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public Result getResult() {
        return result;
    }

    public AsrRsp setResult(Result result) {
        this.result = result;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AsrRsp {\n");

        sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
        sb.append("    result: ").append(toIndentedString(result)).append("\n");
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
        AsrRsp asrRsp = (AsrRsp) o;
        return Objects.equals(this.traceId, asrRsp.traceId) && Objects.equals(this.result, asrRsp.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId, result);
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
