/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ParamExtractionWorkflow
 */

@Validated

public class ParamExtractionWorkflow implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("execution_timing")
    private String executionTiming = null;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private List<WorkflowFieldVO> inputs = null;

    @JsonProperty("outputs")
    @Valid
    @Size()
    private List<WorkflowFieldVO> outputs = null;

    @JsonProperty("memory")
    @Valid
    @Size()
    private List<WorkflowFieldVO> memory = null;

    @JsonProperty("version_id")
    private String versionId = null;

    @JsonProperty("version_name")
    private String versionName = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("entry_condition")
    @Valid
    @Size()
    private Map<String, Object> entryCondition = null;

    public String getId() {
        return id;
    }

    public ParamExtractionWorkflow setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ParamExtractionWorkflow setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ParamExtractionWorkflow setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getExecutionTiming() {
        return executionTiming;
    }

    public ParamExtractionWorkflow setExecutionTiming(String executionTiming) {
        this.executionTiming = executionTiming;
        return this;
    }

    public List<WorkflowFieldVO> getInputs() {
        return inputs;
    }

    public ParamExtractionWorkflow setInputs(List<WorkflowFieldVO> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<WorkflowFieldVO> getOutputs() {
        return outputs;
    }

    public ParamExtractionWorkflow setOutputs(List<WorkflowFieldVO> outputs) {
        this.outputs = outputs;
        return this;
    }

    public List<WorkflowFieldVO> getMemory() {
        return memory;
    }

    public ParamExtractionWorkflow setMemory(List<WorkflowFieldVO> memory) {
        this.memory = memory;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public ParamExtractionWorkflow setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public ParamExtractionWorkflow setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getType() {
        return type;
    }

    public ParamExtractionWorkflow setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getEntryCondition() {
        return entryCondition;
    }

    public ParamExtractionWorkflow setEntryCondition(Map<String, Object> entryCondition) {
        this.entryCondition = entryCondition;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ParamExtractionWorkflow {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    executionTiming: ").append(toIndentedString(executionTiming)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
        sb.append("    memory: ").append(toIndentedString(memory)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    versionName: ").append(toIndentedString(versionName)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    entryCondition: ").append(toIndentedString(entryCondition)).append("\n");
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
        ParamExtractionWorkflow paramExtractionWorkflow = (ParamExtractionWorkflow) o;
        return Objects.equals(this.id, paramExtractionWorkflow.id) && Objects.equals(this.name,
            paramExtractionWorkflow.name) && Objects.equals(this.description, paramExtractionWorkflow.description)
            && Objects.equals(this.executionTiming, paramExtractionWorkflow.executionTiming) && Objects.equals(
            this.inputs, paramExtractionWorkflow.inputs) && Objects.equals(this.outputs,
            paramExtractionWorkflow.outputs) && Objects.equals(this.memory, paramExtractionWorkflow.memory)
            && Objects.equals(this.versionId, paramExtractionWorkflow.versionId) && Objects.equals(this.versionName,
            paramExtractionWorkflow.versionName) && Objects.equals(this.type, paramExtractionWorkflow.type)
            && Objects.equals(this.entryCondition, paramExtractionWorkflow.entryCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, executionTiming, inputs, outputs, memory, versionId, versionName,
            type, entryCondition);
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
