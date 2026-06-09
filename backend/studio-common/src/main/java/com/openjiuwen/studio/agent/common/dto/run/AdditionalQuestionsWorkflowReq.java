/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 自动追问请求体
 */
@ApiModel(description = "自动追问请求体")

@Validated

public class AdditionalQuestionsWorkflowReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    @NotBlank
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("enable")
    @NotNull
    private Boolean enable = null;

    @JsonProperty("prompt")
    private String prompt = null;

    @JsonProperty("version_id")
    private String versionId = null;

    @JsonProperty("model_id")
    private String modelId = null;

    @JsonProperty("model_name")
    private String modelName = null;

    @JsonProperty("model_type")
    private String modelType = null;

    public String getName() {
        return name;
    }

    public AdditionalQuestionsWorkflowReq setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdditionalQuestionsWorkflowReq setDescription(String description) {
        this.description = description;
        return this;
    }

    public AdditionalQuestionsWorkflowReq setEnable(Boolean enable) {
        this.enable = enable;
        return this;
    }

    public Boolean isEnable() {
        return enable;
    }

    public String getPrompt() {
        return prompt;
    }

    public AdditionalQuestionsWorkflowReq setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public AdditionalQuestionsWorkflowReq setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public String getModelId() {
        return modelId;
    }

    public AdditionalQuestionsWorkflowReq setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public String getModelName() {
        return modelName;
    }

    public AdditionalQuestionsWorkflowReq setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getModelType() {
        return modelType;
    }

    public AdditionalQuestionsWorkflowReq setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdditionalQuestionsWorkflowReq {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    enable: ").append(toIndentedString(enable)).append("\n");
        sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    modelId: ").append(toIndentedString(modelId)).append("\n");
        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
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
        AdditionalQuestionsWorkflowReq additionalQuestionsWorkflowReq = (AdditionalQuestionsWorkflowReq) o;
        return Objects.equals(this.name, additionalQuestionsWorkflowReq.name) && Objects.equals(this.description,
            additionalQuestionsWorkflowReq.description) && Objects.equals(this.enable,
            additionalQuestionsWorkflowReq.enable) && Objects.equals(this.prompt, additionalQuestionsWorkflowReq.prompt)
            && Objects.equals(this.versionId, additionalQuestionsWorkflowReq.versionId) && Objects.equals(this.modelId,
            additionalQuestionsWorkflowReq.modelId) && Objects.equals(this.modelName,
            additionalQuestionsWorkflowReq.modelName) && Objects.equals(this.modelType,
            additionalQuestionsWorkflowReq.modelType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, enable, prompt, versionId, modelId, modelName, modelType);
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
