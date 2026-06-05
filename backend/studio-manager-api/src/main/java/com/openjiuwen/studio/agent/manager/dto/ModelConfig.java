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
 * 大模型参数配置。
 */
@ApiModel(description = "大模型参数配置。")

@Validated

public class ModelConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("top_p")
    private Double topP = 1.0d;

    @JsonProperty("temperature")
    private Double temperature = 0.0d;

    @JsonProperty("history_size")
    private Integer historySize = 20;

    @JsonProperty("output_format")
    private String outputFormat = "text";

    @JsonProperty("max_tokens")
    private Integer maxTokens = 4096;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty = 0.0d;

    @JsonProperty("enable_thinking")
    private Boolean enableThinking = true;

    public Double getTopP() {
        return topP;
    }

    public ModelConfig setTopP(Double topP) {
        this.topP = topP;
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public ModelConfig setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Integer getHistorySize() {
        return historySize;
    }

    public ModelConfig setHistorySize(Integer historySize) {
        this.historySize = historySize;
        return this;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public ModelConfig setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public ModelConfig setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public ModelConfig setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    public ModelConfig setEnableThinking(Boolean enableThinking) {
        this.enableThinking = enableThinking;
        return this;
    }

    public Boolean isEnableThinking() {
        return enableThinking;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelConfig {\n");

        sb.append("    topP: ").append(toIndentedString(topP)).append("\n");
        sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
        sb.append("    historySize: ").append(toIndentedString(historySize)).append("\n");
        sb.append("    outputFormat: ").append(toIndentedString(outputFormat)).append("\n");
        sb.append("    maxTokens: ").append(toIndentedString(maxTokens)).append("\n");
        sb.append("    frequencyPenalty: ").append(toIndentedString(frequencyPenalty)).append("\n");
        sb.append("    enableThinking: ").append(toIndentedString(enableThinking)).append("\n");
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
        ModelConfig modelConfig = (ModelConfig) o;
        return Objects.equals(this.topP, modelConfig.topP) && Objects.equals(this.temperature, modelConfig.temperature)
            && Objects.equals(this.historySize, modelConfig.historySize) && Objects.equals(this.outputFormat,
            modelConfig.outputFormat) && Objects.equals(this.maxTokens, modelConfig.maxTokens) && Objects.equals(
            this.frequencyPenalty, modelConfig.frequencyPenalty) && Objects.equals(this.enableThinking,
            modelConfig.enableThinking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topP, temperature, historySize, outputFormat, maxTokens, frequencyPenalty, enableThinking);
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
