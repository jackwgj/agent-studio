/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 工作流字段ir。
 */
@ApiModel(description = "工作流字段ir。")

@Validated

public class WorkflowFieldIR implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("required")
    private Boolean required = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("schema")
    @Valid
    private Object schema = null;

    @JsonProperty("default_value")
    @Valid
    private Object defaultValue = null;

    @JsonProperty("method")
    private String method = null;

    @JsonProperty("visible")
    private Boolean visible = null;

    public String getName() {
        return name;
    }

    public WorkflowFieldIR setName(String name) {
        this.name = name;
        return this;
    }

    public WorkflowFieldIR setRequired(Boolean required) {
        this.required = required;
        return this;
    }

    public Boolean isRequired() {
        return required;
    }

    public String getType() {
        return type;
    }

    public WorkflowFieldIR setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowFieldIR setDescription(String description) {
        this.description = description;
        return this;
    }

    public Object getSchema() {
        return schema;
    }

    public WorkflowFieldIR setSchema(Object schema) {
        this.schema = schema;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public WorkflowFieldIR setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public WorkflowFieldIR setMethod(String method) {
        this.method = method;
        return this;
    }

    public WorkflowFieldIR setVisible(Boolean visible) {
        this.visible = visible;
        return this;
    }

    public Boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowFieldIR {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    required: ").append(toIndentedString(required)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    schema: ").append(toIndentedString(schema)).append("\n");
        sb.append("    defaultValue: ").append(toIndentedString(defaultValue)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    visible: ").append(toIndentedString(visible)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WorkflowFieldIR workflowFieldIR = (WorkflowFieldIR) obj;
        return Objects.equals(this.name, workflowFieldIR.name) && Objects.equals(this.required,
            workflowFieldIR.required) && Objects.equals(this.type, workflowFieldIR.type) && Objects.equals(
            this.description, workflowFieldIR.description) && Objects.equals(this.schema, workflowFieldIR.schema)
            && Objects.equals(this.defaultValue, workflowFieldIR.defaultValue) && Objects.equals(this.method,
            workflowFieldIR.method) && Objects.equals(this.visible, workflowFieldIR.visible);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, required, type, description, schema, defaultValue, method, visible);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
