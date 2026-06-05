/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * MemoryUpdate
 */

@Validated

public class MemoryUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_id")
    private String memoryId = null;

    @JsonProperty("content")
    private String content = null;

    public String getMemoryId() {
        return memoryId;
    }

    public MemoryUpdate setMemoryId(String memoryId) {
        this.memoryId = memoryId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MemoryUpdate setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MemoryUpdate {\n");

        sb.append("    memoryId: ").append(toIndentedString(memoryId)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
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
        MemoryUpdate memoryUpdate = (MemoryUpdate) o;
        return Objects.equals(this.memoryId, memoryUpdate.memoryId) && Objects.equals(this.content,
            memoryUpdate.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryId, content);
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
