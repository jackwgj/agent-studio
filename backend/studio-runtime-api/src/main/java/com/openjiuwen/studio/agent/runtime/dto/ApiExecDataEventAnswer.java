/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 插件调用回答信息
 */
@ApiModel(description = "插件调用回答信息")

@Validated

public class ApiExecDataEventAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("role")
    private String role = null;

    @JsonProperty("name")
    private String name = "agent";

    @JsonProperty("content")
    @Valid
    private Object content = null;

    @JsonProperty("time_consumption")
    @Valid
    private TimeConsumption timeConsumption = null;

    public String getRole() {
        return role;
    }

    public ApiExecDataEventAnswer setRole(String role) {
        this.role = role;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApiExecDataEventAnswer setName(String name) {
        this.name = name;
        return this;
    }

    public Object getContent() {
        return content;
    }

    public ApiExecDataEventAnswer setContent(Object content) {
        this.content = content;
        return this;
    }

    public TimeConsumption getTimeConsumption() {
        return timeConsumption;
    }

    public ApiExecDataEventAnswer setTimeConsumption(TimeConsumption timeConsumption) {
        this.timeConsumption = timeConsumption;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApiExecDataEventAnswer {\n");

        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    timeConsumption: ").append(toIndentedString(timeConsumption)).append("\n");
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
        ApiExecDataEventAnswer apiExecDataEventAnswer = (ApiExecDataEventAnswer) o;
        return Objects.equals(this.role, apiExecDataEventAnswer.role) && Objects.equals(this.name,
            apiExecDataEventAnswer.name) && Objects.equals(this.content, apiExecDataEventAnswer.content)
            && Objects.equals(this.timeConsumption, apiExecDataEventAnswer.timeConsumption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, name, content, timeConsumption);
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
