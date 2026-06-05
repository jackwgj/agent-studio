/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 自动添加资源请求体。
 */
@ApiModel(description = "自动添加资源请求体。")

@Validated

public class AutoAddStudioResourceRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("instructions")
    private String instructions = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("limit")
    private Integer limit = null;

    @JsonProperty("confidence")
    private Double confidence = 0.85d;

    public String getInstructions() {
        return instructions;
    }

    public AutoAddStudioResourceRequestBody setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    public String getType() {
        return type;
    }

    public AutoAddStudioResourceRequestBody setType(String type) {
        this.type = type;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public AutoAddStudioResourceRequestBody setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Double getConfidence() {
        return confidence;
    }

    public AutoAddStudioResourceRequestBody setConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AutoAddStudioResourceRequestBody {\n");

        sb.append("    instructions: ").append(toIndentedString(instructions)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
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
        AutoAddStudioResourceRequestBody autoAddStudioResourceRequestBody = (AutoAddStudioResourceRequestBody) o;
        return Objects.equals(this.instructions, autoAddStudioResourceRequestBody.instructions) && Objects.equals(
            this.type, autoAddStudioResourceRequestBody.type) && Objects.equals(this.limit,
            autoAddStudioResourceRequestBody.limit) && Objects.equals(this.confidence,
            autoAddStudioResourceRequestBody.confidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instructions, type, limit, confidence);
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
