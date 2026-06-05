/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model
 */
@ApiModel(description = "Model")

@Validated
public class ModelProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("model")
    private String model = null;

    @JsonProperty("model_source")
    private String modelSource = null;

    public String getModel() {
        return model;
    }

    public ModelProperties setModel(String model) {
        this.model = model;
        return this;
    }

    public String getModelSource() {
        return modelSource;
    }

    public ModelProperties setModelSource(String modelSource) {
        this.modelSource = modelSource;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelProperties {\n");
        sb.append("    model: ").append(toIndentedString(model)).append("\n");
        sb.append("    modelSource: ").append(toIndentedString(modelSource)).append("\n");
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
        ModelProperties modelProperties = (ModelProperties) o;
        return Objects.equals(this.model, modelProperties.model) && Objects.equals(this.modelSource,
            modelProperties.modelSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, modelSource);
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
