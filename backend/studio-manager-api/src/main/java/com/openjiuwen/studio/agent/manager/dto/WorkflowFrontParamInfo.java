/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 工作流前端参数界面返回值。
 */
@ApiModel(description = "工作流前端参数界面返回值。")

@Validated

public class WorkflowFrontParamInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private List<WorkflowFrontParam> inputs = null;

    @JsonProperty("outputs")
    @Valid
    @Size()
    private List<WorkflowFrontParam> outputs = null;

    @JsonProperty("prologue")
    private String prologue = null;

    @JsonProperty("suggest_queries")
    @Valid
    @Size()
    private List<@Length() String> suggestQueries = null;

    public String getId() {
        return id;
    }

    public WorkflowFrontParamInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkflowFrontParamInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowFrontParamInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public WorkflowFrontParamInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public WorkflowFrontParamInfo setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public List<WorkflowFrontParam> getInputs() {
        return inputs;
    }

    public WorkflowFrontParamInfo setInputs(List<WorkflowFrontParam> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<WorkflowFrontParam> getOutputs() {
        return outputs;
    }

    public WorkflowFrontParamInfo setOutputs(List<WorkflowFrontParam> outputs) {
        this.outputs = outputs;
        return this;
    }

    public String getPrologue() {
        return prologue;
    }

    public WorkflowFrontParamInfo setPrologue(String prologue) {
        this.prologue = prologue;
        return this;
    }

    public List<String> getSuggestQueries() {
        return suggestQueries;
    }

    public WorkflowFrontParamInfo setSuggestQueries(List<String> suggestQueries) {
        this.suggestQueries = suggestQueries;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowFrontParamInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
        sb.append("    prologue: ").append(toIndentedString(prologue)).append("\n");
        sb.append("    suggestQueries: ").append(toIndentedString(suggestQueries)).append("\n");
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
        WorkflowFrontParamInfo workflowFrontParamInfo = (WorkflowFrontParamInfo) o;
        return Objects.equals(this.id, workflowFrontParamInfo.id) && Objects.equals(this.name,
            workflowFrontParamInfo.name) && Objects.equals(this.description, workflowFrontParamInfo.description)
            && Objects.equals(this.type, workflowFrontParamInfo.type) && Objects.equals(this.icon,
            workflowFrontParamInfo.icon) && Objects.equals(this.inputs, workflowFrontParamInfo.inputs)
            && Objects.equals(this.outputs, workflowFrontParamInfo.outputs) && Objects.equals(this.prologue,
            workflowFrontParamInfo.prologue) && Objects.equals(this.suggestQueries,
            workflowFrontParamInfo.suggestQueries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, icon, inputs, outputs, prologue, suggestQueries);
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
