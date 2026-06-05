/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 评估任务DTO
 */
@ApiModel(description = "评估任务DTO")

@Validated
public class PeEvaluationTaskDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5\\w-]{0,30}[\\u4e00-\\u9fa5a-zA-Z0-9]$")
    @NotBlank
    private String name = null;

    @JsonProperty("description")
    @Length(max = 100)
    private String description = null;

    @JsonProperty("task_id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    @NotBlank
    private String taskId = null;

    @JsonProperty("method")
    @Pattern(regexp = "similarity|matching")
    @NotBlank
    private String method = null;

    @JsonProperty("test_set_id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    @NotBlank
    private String testSetId = null;

    @JsonProperty("prompt_ids")
    @Valid
    @NotNull
    @Size(max = 9)
    private List<@Pattern(
        regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$") @Length() String> promptIds
        = new ArrayList<String>();

    @JsonProperty("regular_expression")
    @Length(max = 64)
    private String regularExpression = null;

    @JsonProperty("eval_model_config")
    @Valid
    private ModelConfig evalModelConfig = null;

    public String getName() {
        return name;
    }

    public PeEvaluationTaskDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PeEvaluationTaskDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public PeEvaluationTaskDto setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public PeEvaluationTaskDto setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getTestSetId() {
        return testSetId;
    }

    public PeEvaluationTaskDto setTestSetId(String testSetId) {
        this.testSetId = testSetId;
        return this;
    }

    public List<String> getPromptIds() {
        return promptIds;
    }

    public PeEvaluationTaskDto setPromptIds(List<String> promptIds) {
        this.promptIds = promptIds;
        return this;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public PeEvaluationTaskDto setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
        return this;
    }

    public ModelConfig getEvalModelConfig() {
        return evalModelConfig;
    }

    public PeEvaluationTaskDto setEvalModelConfig(ModelConfig evalModelConfig) {
        this.evalModelConfig = evalModelConfig;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeEvaluationTaskDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    testSetId: ").append(toIndentedString(testSetId)).append("\n");
        sb.append("    promptIds: ").append(toIndentedString(promptIds)).append("\n");
        sb.append("    regularExpression: ").append(toIndentedString(regularExpression)).append("\n");
        sb.append("    evalModelConfig: ").append(toIndentedString(evalModelConfig)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PeEvaluationTaskDto peEvaluationTaskDto = (PeEvaluationTaskDto) obj;
        return Objects.equals(this.name, peEvaluationTaskDto.name) && Objects.equals(this.description,
            peEvaluationTaskDto.description) && Objects.equals(this.taskId, peEvaluationTaskDto.taskId)
            && Objects.equals(this.method, peEvaluationTaskDto.method) && Objects.equals(this.testSetId,
            peEvaluationTaskDto.testSetId) && Objects.equals(this.promptIds, peEvaluationTaskDto.promptIds)
            && Objects.equals(this.regularExpression, peEvaluationTaskDto.regularExpression) && Objects.equals(
            this.evalModelConfig, peEvaluationTaskDto.evalModelConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, taskId, method, testSetId, promptIds, regularExpression,
            evalModelConfig);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
