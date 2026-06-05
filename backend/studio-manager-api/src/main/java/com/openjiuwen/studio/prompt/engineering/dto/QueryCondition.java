/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * QueryCondition
 */

@Validated
public class QueryCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("task_id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    @NotBlank
    private String taskId = null;

    @JsonProperty("name")
    @Length(max = 255)
    private String name = null;

    @JsonProperty("content")
    @Length(max = 10000)
    private String content = null;

    @JsonProperty("is_good_rating")
    private Boolean isGoodRating = false;

    @JsonProperty("type")
    private Integer type = null;

    @JsonProperty("file_id")
    private String fileId = null;

    public String getTaskId() {
        return taskId;
    }

    public QueryCondition setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getName() {
        return name;
    }

    public QueryCondition setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public QueryCondition setContent(String content) {
        this.content = content;
        return this;
    }

    public QueryCondition setIsGoodRating(Boolean isGoodRating) {
        this.isGoodRating = isGoodRating;
        return this;
    }

    public Boolean isIsGoodRating() {
        return isGoodRating;
    }

    public Integer getType() {
        return type;
    }

    public QueryCondition setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getFileId() {
        return fileId;
    }

    public QueryCondition setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueryCondition {\n");
        sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    isGoodRating: ").append(toIndentedString(isGoodRating)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    fileId: ").append(toIndentedString(fileId)).append("\n");
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
        QueryCondition queryCondition = (QueryCondition) o;
        return Objects.equals(this.taskId, queryCondition.taskId) && Objects.equals(this.name, queryCondition.name)
            && Objects.equals(this.content, queryCondition.content) && Objects.equals(this.isGoodRating,
            queryCondition.isGoodRating) && Objects.equals(this.type, queryCondition.type) && Objects.equals(
            this.fileId, queryCondition.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, name, content, isGoodRating, type, fileId);
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
