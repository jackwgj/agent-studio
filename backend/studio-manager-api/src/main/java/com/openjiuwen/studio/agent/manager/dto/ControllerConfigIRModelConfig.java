/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ControllerConfigIRModelConfig
 */

@Validated

public class ControllerConfigIRModelConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("modelName")
    private String modelName = null;

    @JsonProperty("modelType")
    private String modelType = null;

    @JsonProperty("hyperParameters")
    @Valid
    private ControllerConfigIRModelConfigHyperParameters hyperParameters = null;

    @JsonProperty("extension")
    @Valid
    private Object extension = null;

    @JsonProperty("deploymentId")
    private String deploymentId = null;

    public String getModelName() {
        return modelName;
    }

    public ControllerConfigIRModelConfig setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getModelType() {
        return modelType;
    }

    public ControllerConfigIRModelConfig setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }

    public ControllerConfigIRModelConfigHyperParameters getHyperParameters() {
        return hyperParameters;
    }

    public ControllerConfigIRModelConfig setHyperParameters(
        ControllerConfigIRModelConfigHyperParameters hyperParameters) {
        this.hyperParameters = hyperParameters;
        return this;
    }

    public Object getExtension() {
        return extension;
    }

    public ControllerConfigIRModelConfig setExtension(Object extension) {
        this.extension = extension;
        return this;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public ControllerConfigIRModelConfig setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllerConfigIRModelConfig {\n");

        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
        sb.append("    hyperParameters: ").append(toIndentedString(hyperParameters)).append("\n");
        sb.append("    extension: ").append(toIndentedString(extension)).append("\n");
        sb.append("    deploymentId: ").append(toIndentedString(deploymentId)).append("\n");
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
        ControllerConfigIRModelConfig controllerConfigIRModelConfig = (ControllerConfigIRModelConfig) o;
        return Objects.equals(this.modelName, controllerConfigIRModelConfig.modelName) && Objects.equals(this.modelType,
            controllerConfigIRModelConfig.modelType) && Objects.equals(this.hyperParameters,
            controllerConfigIRModelConfig.hyperParameters) && Objects.equals(this.extension,
            controllerConfigIRModelConfig.extension) && Objects.equals(this.deploymentId,
            controllerConfigIRModelConfig.deploymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName, modelType, hyperParameters, extension, deploymentId);
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
