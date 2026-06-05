/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 大模型参数配置
 */
@ApiModel(description = "大模型参数配置")

@Validated
public class ExecConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("model")
    @Length(max = 128)
    private String model = null;

    @JsonProperty("exec_type")
    private ExecTypeEnum execType = null;

    @JsonProperty("top_p")
    private Double topP = 1.0d;

    @JsonProperty("temperature")
    private Double temperature = 0.0d;

    @JsonProperty("max_tokens")
    private Integer maxTokens = null;

    public String getModel() {
        return model;
    }

    public ExecConfig setModel(String model) {
        this.model = model;
        return this;
    }

    public ExecTypeEnum getExecType() {
        return execType;
    }

    public ExecConfig setExecType(ExecTypeEnum execType) {
        this.execType = execType;
        return this;
    }

    public Double getTopP() {
        return topP;
    }

    public ExecConfig setTopP(Double topP) {
        this.topP = topP;
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public ExecConfig setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public ExecConfig setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExecConfig {\n");
        sb.append("    model: ").append(toIndentedString(model)).append("\n");
        sb.append("    execType: ").append(toIndentedString(execType)).append("\n");
        sb.append("    topP: ").append(toIndentedString(topP)).append("\n");
        sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
        sb.append("    maxTokens: ").append(toIndentedString(maxTokens)).append("\n");
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
        ExecConfig execConfig = (ExecConfig) o;
        return Objects.equals(this.model, execConfig.model) && Objects.equals(this.execType, execConfig.execType)
            && Objects.equals(this.topP, execConfig.topP) && Objects.equals(this.temperature, execConfig.temperature)
            && Objects.equals(this.maxTokens, execConfig.maxTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, execType, topP, temperature, maxTokens);
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

    /**
     * 模型或者智能体id
     */
    public enum ExecTypeEnum {

        MODEL("model"),
        AGENT("agent");

        private String value;

        ExecTypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExecTypeEnum fromValue(String text) {
            for (ExecTypeEnum b : ExecTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
