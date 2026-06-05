/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Objects;

/**
 * 自然语言的长期记忆提取信息
 *
 */
@Data
public class NaturalLanguageMemory {

    @JsonProperty("memory_id")
    private String memoryId;

    @JsonProperty("content")
    private String content;

    /**
     * 记忆内容的操作类型，UPDATE/DELETE
     */
    @JsonProperty("operation")
    private String operation;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NaturalLanguageMemory that = (NaturalLanguageMemory) o;
        return Objects.equals(memoryId, that.memoryId) && Objects.equals(content, that.content) && Objects.equals(
            operation, that.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryId, content, operation);
    }
}
