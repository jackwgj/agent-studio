/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * McpCallToolRespContent
 */

@Validated

public class McpCallToolRespContent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("text")
    private String text = null;

    public String getType() {
        return type;
    }

    public McpCallToolRespContent setType(String type) {
        this.type = type;
        return this;
    }

    public String getText() {
        return text;
    }

    public McpCallToolRespContent setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpCallToolRespContent {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
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
        McpCallToolRespContent mcpCallToolRespContent = (McpCallToolRespContent) o;
        return Objects.equals(this.type, mcpCallToolRespContent.type) && Objects.equals(this.text,
            mcpCallToolRespContent.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text);
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
