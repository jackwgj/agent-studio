/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 优化任务Vo
 */
@ApiModel(description = "优化任务Vo")

@Validated
public class PeOptimizationTaskVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("jiuwen_task_id")
    private String jiuwenTaskId = null;

    @JsonProperty("task_id")
    private String taskId = null;

    @JsonProperty("test_set_id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String testSetId = null;

    @JsonProperty("model_config")
    @Valid
    private ModelConfig modelConfig = null;

    @JsonProperty("assistant_config")
    @Valid
    private ModelConfig assistantConfig = null;

    @JsonProperty("num_iter")
    private Integer numIter = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("progress_rate")
    private Double progressRate = null;

    @JsonProperty("best_prompt")
    private String bestPrompt = null;

    @JsonProperty("error_msg")
    private String errorMsg = null;

    @JsonProperty("created_on")
    private String createdOn = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("updated_on")
    private String updatedOn = null;

    @JsonProperty("running_on")
    private String runningOn = null;

    @JsonProperty("prompts")
    @Valid
    private List<PePromptVo> prompts = null;

    public String getId() {
        return id;
    }

    public PeOptimizationTaskVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PeOptimizationTaskVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PeOptimizationTaskVo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public PeOptimizationTaskVo setType(String type) {
        this.type = type;
        return this;
    }

    public String getJiuwenTaskId() {
        return jiuwenTaskId;
    }

    public PeOptimizationTaskVo setJiuwenTaskId(String jiuwenTaskId) {
        this.jiuwenTaskId = jiuwenTaskId;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public PeOptimizationTaskVo setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getTestSetId() {
        return testSetId;
    }

    public PeOptimizationTaskVo setTestSetId(String testSetId) {
        this.testSetId = testSetId;
        return this;
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public PeOptimizationTaskVo setModelConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        return this;
    }

    public ModelConfig getAssistantConfig() {
        return assistantConfig;
    }

    public PeOptimizationTaskVo setAssistantConfig(ModelConfig assistantConfig) {
        this.assistantConfig = assistantConfig;
        return this;
    }

    public Integer getNumIter() {
        return numIter;
    }

    public PeOptimizationTaskVo setNumIter(Integer numIter) {
        this.numIter = numIter;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PeOptimizationTaskVo setStatus(String status) {
        this.status = status;
        return this;
    }

    public Double getProgressRate() {
        return progressRate;
    }

    public PeOptimizationTaskVo setProgressRate(Double progressRate) {
        this.progressRate = progressRate;
        return this;
    }

    public String getBestPrompt() {
        return bestPrompt;
    }

    public PeOptimizationTaskVo setBestPrompt(String bestPrompt) {
        this.bestPrompt = bestPrompt;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public PeOptimizationTaskVo setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public PeOptimizationTaskVo setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public PeOptimizationTaskVo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public PeOptimizationTaskVo setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    public String getRunningOn() {
        return runningOn;
    }

    public PeOptimizationTaskVo setRunningOn(String runningOn) {
        this.runningOn = runningOn;
        return this;
    }

    public List<PePromptVo> getPrompts() {
        return prompts;
    }

    public PeOptimizationTaskVo setPrompts(List<PePromptVo> prompts) {
        this.prompts = prompts;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeOptimizationTaskVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    jiuwenTaskId: ").append(toIndentedString(jiuwenTaskId)).append("\n");
        sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
        sb.append("    testSetId: ").append(toIndentedString(testSetId)).append("\n");
        sb.append("    modelConfig: ").append(toIndentedString(modelConfig)).append("\n");
        sb.append("    assistantConfig: ").append(toIndentedString(assistantConfig)).append("\n");
        sb.append("    numIter: ").append(toIndentedString(numIter)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    progressRate: ").append(toIndentedString(progressRate)).append("\n");
        sb.append("    bestPrompt: ").append(toIndentedString(bestPrompt)).append("\n");
        sb.append("    errorMsg: ").append(toIndentedString(errorMsg)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("    runningOn: ").append(toIndentedString(runningOn)).append("\n");
        sb.append("    prompts: ").append(toIndentedString(prompts)).append("\n");
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
        PeOptimizationTaskVo peOptimizationTaskVo = (PeOptimizationTaskVo) o;
        return Objects.equals(this.id, peOptimizationTaskVo.id) && Objects.equals(this.name, peOptimizationTaskVo.name)
            && Objects.equals(this.description, peOptimizationTaskVo.description) && Objects.equals(this.type,
            peOptimizationTaskVo.type) && Objects.equals(this.jiuwenTaskId, peOptimizationTaskVo.jiuwenTaskId)
            && Objects.equals(this.taskId, peOptimizationTaskVo.taskId) && Objects.equals(this.testSetId,
            peOptimizationTaskVo.testSetId) && Objects.equals(this.modelConfig, peOptimizationTaskVo.modelConfig)
            && Objects.equals(this.assistantConfig, peOptimizationTaskVo.assistantConfig) && Objects.equals(
            this.numIter, peOptimizationTaskVo.numIter) && Objects.equals(this.status, peOptimizationTaskVo.status)
            && Objects.equals(this.progressRate, peOptimizationTaskVo.progressRate) && Objects.equals(this.bestPrompt,
            peOptimizationTaskVo.bestPrompt) && Objects.equals(this.errorMsg, peOptimizationTaskVo.errorMsg)
            && Objects.equals(this.createdOn, peOptimizationTaskVo.createdOn) && Objects.equals(this.creator,
            peOptimizationTaskVo.creator) && Objects.equals(this.updatedOn, peOptimizationTaskVo.updatedOn)
            && Objects.equals(this.runningOn, peOptimizationTaskVo.runningOn) && Objects.equals(this.prompts,
            peOptimizationTaskVo.prompts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, jiuwenTaskId, taskId, testSetId, modelConfig, assistantConfig,
            numIter, status, progressRate, bestPrompt, errorMsg, createdOn, creator, updatedOn, runningOn, prompts);
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
