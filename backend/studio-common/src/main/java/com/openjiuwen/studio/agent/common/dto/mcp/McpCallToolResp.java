/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * mcp 服务工具调用结果
 */
@ApiModel(description = "mcp 服务工具调用结果")
@Validated
public class McpCallToolResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("isError")
    private Boolean isError = null;

    @JsonProperty("content")
    @Valid
    @Size()
    private List<McpCallToolRespContent> content = null;

    public McpCallToolResp setIsError(Boolean isError) {
        this.isError = isError;
        return this;
    }

    public Boolean isIsError() {
        return isError;
    }

    public List<McpCallToolRespContent> getContent() {
        return content;
    }

    public McpCallToolResp setContent(List<McpCallToolRespContent> content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpCallToolResp {\n");

        sb.append("    isError: ").append(toIndentedString(isError)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
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
        McpCallToolResp mcpCallToolResp = (McpCallToolResp) o;
        return Objects.equals(this.isError, mcpCallToolResp.isError) && Objects.equals(this.content,
            mcpCallToolResp.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isError, content);
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