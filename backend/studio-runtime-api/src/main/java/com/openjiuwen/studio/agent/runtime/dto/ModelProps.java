/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ModelProps
 */

@Validated

public class ModelProps implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("model_name")
    private String modelName = null;

    @JsonProperty("model_id")
    private String modelId = null;

    @JsonProperty("model_provider")
    private String modelProvider = null;

    @JsonProperty("call_options")
    private String callOptions = null;

    @JsonProperty("error")
    private String error = null;

    public String getModelName() {
        return modelName;
    }

    public ModelProps setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getModelId() {
        return modelId;
    }

    public ModelProps setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public ModelProps setModelProvider(String modelProvider) {
        this.modelProvider = modelProvider;
        return this;
    }

    public String getCallOptions() {
        return callOptions;
    }

    public ModelProps setCallOptions(String callOptions) {
        this.callOptions = callOptions;
        return this;
    }

    public String getError() {
        return error;
    }

    public ModelProps setError(String error) {
        this.error = error;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelProps {\n");

        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelId: ").append(toIndentedString(modelId)).append("\n");
        sb.append("    modelProvider: ").append(toIndentedString(modelProvider)).append("\n");
        sb.append("    callOptions: ").append(toIndentedString(callOptions)).append("\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
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
        ModelProps modelProps = (ModelProps) o;
        return Objects.equals(this.modelName, modelProps.modelName) && Objects.equals(this.modelId, modelProps.modelId)
            && Objects.equals(this.modelProvider, modelProps.modelProvider) && Objects.equals(this.callOptions,
            modelProps.callOptions) && Objects.equals(this.error, modelProps.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName, modelId, modelProvider, callOptions, error);
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
