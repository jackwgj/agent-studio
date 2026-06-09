/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 全局意图工作流IR对象。
 */
@ApiModel(description = "全局意图工作流IR对象。")

@Validated

public class IntentWorkflowVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("version")
    private String version = null;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private List<WorkflowFieldVO> inputs = null;

    @JsonProperty("outputs")
    @Valid
    @Size()
    private List<WorkflowFieldVO> outputs = null;

    public String getId() {
        return id;
    }

    public IntentWorkflowVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IntentWorkflowVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public IntentWorkflowVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public IntentWorkflowVO setVersion(String version) {
        this.version = version;
        return this;
    }

    public List<WorkflowFieldVO> getInputs() {
        return inputs;
    }

    public IntentWorkflowVO setInputs(List<WorkflowFieldVO> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<WorkflowFieldVO> getOutputs() {
        return outputs;
    }

    public IntentWorkflowVO setOutputs(List<WorkflowFieldVO> outputs) {
        this.outputs = outputs;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IntentWorkflowVO {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
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
        IntentWorkflowVO intentWorkflowVO = (IntentWorkflowVO) o;
        return Objects.equals(this.id, intentWorkflowVO.id) && Objects.equals(this.name, intentWorkflowVO.name)
            && Objects.equals(this.description, intentWorkflowVO.description) && Objects.equals(this.version,
            intentWorkflowVO.version) && Objects.equals(this.inputs, intentWorkflowVO.inputs) && Objects.equals(
            this.outputs, intentWorkflowVO.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, version, inputs, outputs);
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
