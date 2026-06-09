/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 工作流通用字段定义，用于inputs,outputs,condition。
 */
@ApiModel(description = "工作流通用字段定义，用于inputs,outputs,condition。")

@Validated

public class WorkflowFieldVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    @NotBlank
    private String name = null;

    @JsonProperty("cn_name")
    private String cnName = null;

    @JsonProperty("type")
    @NotBlank
    private String type = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("required")
    private Boolean required = null;

    @JsonProperty("source")
    private SourceEnum source = SourceEnum.USER;

    @JsonProperty("schema")
    @Valid
    private Object schema = null;

    @JsonProperty("aging_level")
    private String agingLevel = null;

    @JsonProperty("storage_method")
    private String storageMethod = null;

    @JsonProperty("field_type")
    private String fieldType = null;

    @JsonProperty("reflection")
    private Boolean reflection = false;

    @JsonProperty("value")
    @Valid
    private WorkflowFieldVOValue value = null;

    @JsonProperty("validator")
    @Valid
    @Size()
    private List<WorkflowFieldVOValidator> validator = null;

    @JsonProperty("memory")
    @Valid
    @Size()
    private List<WorkflowFieldVOMemory> memory = null;

    public String getName() {
        return name;
    }

    public WorkflowFieldVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getCnName() {
        return cnName;
    }

    public WorkflowFieldVO setCnName(String cnName) {
        this.cnName = cnName;
        return this;
    }

    public String getType() {
        return type;
    }

    public WorkflowFieldVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowFieldVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public WorkflowFieldVO setRequired(Boolean required) {
        this.required = required;
        return this;
    }

    public Boolean isRequired() {
        return required;
    }

    public SourceEnum getSource() {
        return source;
    }

    public WorkflowFieldVO setSource(SourceEnum source) {
        this.source = source;
        return this;
    }

    public Object getSchema() {
        return schema;
    }

    public WorkflowFieldVO setSchema(Object schema) {
        this.schema = schema;
        return this;
    }

    public String getAgingLevel() {
        return agingLevel;
    }

    public WorkflowFieldVO setAgingLevel(String agingLevel) {
        this.agingLevel = agingLevel;
        return this;
    }

    public String getStorageMethod() {
        return storageMethod;
    }

    public WorkflowFieldVO setStorageMethod(String storageMethod) {
        this.storageMethod = storageMethod;
        return this;
    }

    public String getFieldType() {
        return fieldType;
    }

    public WorkflowFieldVO setFieldType(String fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public WorkflowFieldVO setReflection(Boolean reflection) {
        this.reflection = reflection;
        return this;
    }

    public Boolean isReflection() {
        return reflection;
    }

    public WorkflowFieldVOValue getValue() {
        return value;
    }

    public WorkflowFieldVO setValue(WorkflowFieldVOValue value) {
        this.value = value;
        return this;
    }

    public List<WorkflowFieldVOValidator> getValidator() {
        return validator;
    }

    public WorkflowFieldVO setValidator(List<WorkflowFieldVOValidator> validator) {
        this.validator = validator;
        return this;
    }

    public List<WorkflowFieldVOMemory> getMemory() {
        return memory;
    }

    public WorkflowFieldVO setMemory(List<WorkflowFieldVOMemory> memory) {
        this.memory = memory;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowFieldVO {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    cnName: ").append(toIndentedString(cnName)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    required: ").append(toIndentedString(required)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    schema: ").append(toIndentedString(schema)).append("\n");
        sb.append("    agingLevel: ").append(toIndentedString(agingLevel)).append("\n");
        sb.append("    storageMethod: ").append(toIndentedString(storageMethod)).append("\n");
        sb.append("    fieldType: ").append(toIndentedString(fieldType)).append("\n");
        sb.append("    reflection: ").append(toIndentedString(reflection)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    validator: ").append(toIndentedString(validator)).append("\n");
        sb.append("    memory: ").append(toIndentedString(memory)).append("\n");
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
        WorkflowFieldVO workflowFieldVO = (WorkflowFieldVO) o;
        return Objects.equals(this.name, workflowFieldVO.name) && Objects.equals(this.cnName, workflowFieldVO.cnName)
            && Objects.equals(this.type, workflowFieldVO.type) && Objects.equals(this.description,
            workflowFieldVO.description) && Objects.equals(this.required, workflowFieldVO.required) && Objects.equals(
            this.source, workflowFieldVO.source) && Objects.equals(this.schema, workflowFieldVO.schema)
            && Objects.equals(this.agingLevel, workflowFieldVO.agingLevel) && Objects.equals(this.storageMethod,
            workflowFieldVO.storageMethod) && Objects.equals(this.fieldType, workflowFieldVO.fieldType)
            && Objects.equals(this.reflection, workflowFieldVO.reflection) && Objects.equals(this.value,
            workflowFieldVO.value) && Objects.equals(this.validator, workflowFieldVO.validator) && Objects.equals(
            this.memory, workflowFieldVO.memory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cnName, type, description, required, source, schema, agingLevel, storageMethod,
            fieldType, reflection, value, validator, memory);
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

    /**
     * 字段数据来源。
     */
    public enum SourceEnum {
        SYSTEM("system"),

        ENVIRONMENT("environment"),

        USER("user"),

        PRE_DEFINED("pre_defined"),

        REQUEST("request");

        private String value;

        SourceEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static SourceEnum fromValue(String text) {
            for (SourceEnum b : SourceEnum.values()) {
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
