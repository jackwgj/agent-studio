/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * Memory
 */

@Validated

public class Memory implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_id")
    private String memoryId = null;

    @JsonProperty("content")
    private String content = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("last_update_time")
    private Long lastUpdateTime = null;

    public String getMemoryId() {
        return memoryId;
    }

    public Memory setMemoryId(String memoryId) {
        this.memoryId = memoryId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Memory setContent(String content) {
        this.content = content;
        return this;
    }

    public String getType() {
        return type;
    }

    public Memory setType(String type) {
        this.type = type;
        return this;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Memory setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Memory {\n");

        sb.append("    memoryId: ").append(toIndentedString(memoryId)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    lastUpdateTime: ").append(toIndentedString(lastUpdateTime)).append("\n");
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
        Memory memory = (Memory) o;
        return Objects.equals(this.memoryId, memory.memoryId) && Objects.equals(this.content, memory.content)
            && Objects.equals(this.type, memory.type) && Objects.equals(this.lastUpdateTime, memory.lastUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryId, content, type, lastUpdateTime);
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
