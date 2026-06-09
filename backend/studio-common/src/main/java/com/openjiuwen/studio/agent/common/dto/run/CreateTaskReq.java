/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;

/**
 * CreateTaskReq
 */
@Validated
public class CreateTaskReq {
    @JsonProperty("type")
    @Length(max = 64)
    private String type = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("mode")
    @Length(max = 64)
    private String mode = null;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private Map<String, Object> inputs = null;

    @JsonProperty("timeout")
    private Integer timeout = null;

    @JsonProperty("version")
    private Long version = null;

    public String getType() {
        return type;
    }

    public CreateTaskReq setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public CreateTaskReq setName(String name) {
        this.name = name;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public CreateTaskReq setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public CreateTaskReq setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public CreateTaskReq setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public CreateTaskReq setVersion(Long version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreateTaskReq {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    timeout: ").append(toIndentedString(timeout)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
        CreateTaskReq createTaskReq = (CreateTaskReq) o;
        return Objects.equals(this.type, createTaskReq.type) && Objects.equals(this.name, createTaskReq.name)
            && Objects.equals(this.mode, createTaskReq.mode) && Objects.equals(this.inputs, createTaskReq.inputs)
            && Objects.equals(this.timeout, createTaskReq.timeout) && Objects.equals(this.version,
            createTaskReq.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, mode, inputs, timeout, version);
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
