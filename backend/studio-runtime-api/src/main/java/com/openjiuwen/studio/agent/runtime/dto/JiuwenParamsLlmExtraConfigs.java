/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 大模型参数
 */
@ApiModel(description = "大模型参数")

@Validated

public class JiuwenParamsLlmExtraConfigs implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("X-Auth-Token")
    private String xAuthToken = null;

    @JsonProperty("modelMap")
    @Valid
    @Size()
    private Map<String, Object> modelMap = null;

    public String getXAuthToken() {
        return xAuthToken;
    }

    public JiuwenParamsLlmExtraConfigs setXAuthToken(String xAuthToken) {
        this.xAuthToken = xAuthToken;
        return this;
    }

    public Map<String, Object> getModelMap() {
        return modelMap;
    }

    public JiuwenParamsLlmExtraConfigs setModelMap(Map<String, Object> modelMap) {
        this.modelMap = modelMap;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenParamsLlmExtraConfigs {\n");

        sb.append("    xAuthToken: ").append(toIndentedString(xAuthToken)).append("\n");
        sb.append("    modelMap: ").append(toIndentedString(modelMap)).append("\n");
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
        JiuwenParamsLlmExtraConfigs jiuwenParamsLlmExtraConfigs = (JiuwenParamsLlmExtraConfigs) o;
        return Objects.equals(this.xAuthToken, jiuwenParamsLlmExtraConfigs.xAuthToken) && Objects.equals(this.modelMap,
            jiuwenParamsLlmExtraConfigs.modelMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xAuthToken, modelMap);
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
