/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * TokensProps
 */

@Validated

public class TokensProps implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("input_tokens")
    private Integer inputTokens = null;

    @JsonProperty("output_tokens")
    private Integer outputTokens = null;

    @JsonProperty("total_tokens")
    private Integer totalTokens = null;

    public Integer getInputTokens() {
        return inputTokens;
    }

    public TokensProps setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
        return this;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public TokensProps setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
        return this;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public TokensProps setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TokensProps {\n");

        sb.append("    inputTokens: ").append(toIndentedString(inputTokens)).append("\n");
        sb.append("    outputTokens: ").append(toIndentedString(outputTokens)).append("\n");
        sb.append("    totalTokens: ").append(toIndentedString(totalTokens)).append("\n");
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
        TokensProps tokensProps = (TokensProps) o;
        return Objects.equals(this.inputTokens, tokensProps.inputTokens) && Objects.equals(this.outputTokens,
            tokensProps.outputTokens) && Objects.equals(this.totalTokens, tokensProps.totalTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputTokens, outputTokens, totalTokens);
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
