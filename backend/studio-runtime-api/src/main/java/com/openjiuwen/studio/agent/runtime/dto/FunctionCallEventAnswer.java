/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 插件调用请求信息
 */
@ApiModel(description = "插件调用请求信息")

@Validated

public class FunctionCallEventAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("function_call")
    @Valid
    private FunctionCall functionCall = null;

    @JsonProperty("is_workflow")
    private Boolean isWorkflow = null;

    @JsonProperty("time_consumption")
    @Valid
    private TimeConsumption timeConsumption = null;

    @JsonProperty("memory_variables")
    @Valid
    @Size()
    private Map<@Length() String, @Length() String> memoryVariables = null;

    public FunctionCall getFunctionCall() {
        return functionCall;
    }

    public FunctionCallEventAnswer setFunctionCall(FunctionCall functionCall) {
        this.functionCall = functionCall;
        return this;
    }

    public FunctionCallEventAnswer setIsWorkflow(Boolean isWorkflow) {
        this.isWorkflow = isWorkflow;
        return this;
    }

    public Boolean isIsWorkflow() {
        return isWorkflow;
    }

    public TimeConsumption getTimeConsumption() {
        return timeConsumption;
    }

    public FunctionCallEventAnswer setTimeConsumption(TimeConsumption timeConsumption) {
        this.timeConsumption = timeConsumption;
        return this;
    }

    public Map<String, String> getMemoryVariables() {
        return memoryVariables;
    }

    public FunctionCallEventAnswer setMemoryVariables(Map<String, String> memoryVariables) {
        this.memoryVariables = memoryVariables;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FunctionCallEventAnswer {\n");

        sb.append("    functionCall: ").append(toIndentedString(functionCall)).append("\n");
        sb.append("    isWorkflow: ").append(toIndentedString(isWorkflow)).append("\n");
        sb.append("    timeConsumption: ").append(toIndentedString(timeConsumption)).append("\n");
        sb.append("    memoryVariables: ").append(toIndentedString(memoryVariables)).append("\n");
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
        FunctionCallEventAnswer functionCallEventAnswer = (FunctionCallEventAnswer) o;
        return Objects.equals(this.functionCall, functionCallEventAnswer.functionCall) && Objects.equals(
                this.isWorkflow, functionCallEventAnswer.isWorkflow) && Objects.equals(this.timeConsumption,
                functionCallEventAnswer.timeConsumption) && Objects.equals(this.memoryVariables,
                functionCallEventAnswer.memoryVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionCall, isWorkflow, timeConsumption, memoryVariables);
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
