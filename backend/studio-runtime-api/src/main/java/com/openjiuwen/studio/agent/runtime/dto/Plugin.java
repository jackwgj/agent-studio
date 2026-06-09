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
 * 插件调用
 */
@ApiModel(description = "插件调用")

@Validated

public class Plugin implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("arguments")
    private String arguments = null;

    @JsonProperty("memory_variables")
    @Valid
    @Size()
    private Map<@Length() String, @Length() String> memoryVariables = null;

    @JsonProperty("tool_name")
    private String toolName = null;

    public String getName() {
        return name;
    }

    public Plugin setName(String name) {
        this.name = name;
        return this;
    }

    public String getArguments() {
        return arguments;
    }

    public Plugin setArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }

    public Map<String, String> getMemoryVariables() {
        return memoryVariables;
    }

    public Plugin setMemoryVariables(Map<String, String> memoryVariables) {
        this.memoryVariables = memoryVariables;
        return this;
    }

    public String getToolName() {
        return toolName;
    }

    public Plugin setToolName(String toolName) {
        this.toolName = toolName;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Plugin {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    arguments: ").append(toIndentedString(arguments)).append("\n");
        sb.append("    memoryVariables: ").append(toIndentedString(memoryVariables)).append("\n");
        sb.append("    toolName: ").append(toIndentedString(toolName)).append("\n");
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
        Plugin plugin = (Plugin) o;
        return Objects.equals(this.name, plugin.name) && Objects.equals(this.arguments, plugin.arguments)
            && Objects.equals(this.memoryVariables, plugin.memoryVariables) && Objects.equals(this.toolName,
            plugin.toolName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments, memoryVariables, toolName);
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
