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
 * mcp 服务工具列表
 */
@ApiModel(description = "mcp 服务工具列表")

@Validated

public class McpServerTools implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    private Integer count = null;

    @JsonProperty("tools")
    @Valid
    @Size()
    private List<McpServerTool> tools = null;

    public Integer getCount() {
        return count;
    }

    public McpServerTools setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<McpServerTool> getTools() {
        return tools;
    }

    public McpServerTools setTools(List<McpServerTool> tools) {
        this.tools = tools;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpServerTools {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
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
        McpServerTools mcpServerTools = (McpServerTools) o;
        return Objects.equals(this.count, mcpServerTools.count) && Objects.equals(this.tools, mcpServerTools.tools);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, tools);
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
