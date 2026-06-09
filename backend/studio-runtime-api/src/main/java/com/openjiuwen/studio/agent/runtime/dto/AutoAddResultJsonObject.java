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
import java.util.List;
import java.util.Objects;

/**
 * 智能生成（添加）的返回体
 */
@ApiModel(description = "智能生成（添加）的返回体")

@Validated

public class AutoAddResultJsonObject implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("questions")
    @Valid
    @Size()
    private List<@Length() String> questions = null;

    @JsonProperty("memory_chunks")
    @Valid
    @Size()
    private List<MemoryChunk> memoryChunks = null;

    public List<String> getQuestions() {
        return questions;
    }

    public AutoAddResultJsonObject setQuestions(List<String> questions) {
        this.questions = questions;
        return this;
    }

    public List<MemoryChunk> getMemoryChunks() {
        return memoryChunks;
    }

    public AutoAddResultJsonObject setMemoryChunks(List<MemoryChunk> memoryChunks) {
        this.memoryChunks = memoryChunks;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AutoAddResultJsonObject {\n");

        sb.append("    questions: ").append(toIndentedString(questions)).append("\n");
        sb.append("    memoryChunks: ").append(toIndentedString(memoryChunks)).append("\n");
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
        AutoAddResultJsonObject autoAddResultJsonObject = (AutoAddResultJsonObject) o;
        return Objects.equals(this.questions, autoAddResultJsonObject.questions) && Objects.equals(this.memoryChunks,
            autoAddResultJsonObject.memoryChunks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questions, memoryChunks);
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
