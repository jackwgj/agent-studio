/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 模型信息
 */
@ApiModel(description = "模型信息")

@Validated
public class ModelInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("type")
    @NotBlank
    private String type = null;

    @JsonProperty("name")
    @NotBlank
    private String name = null;

    @JsonProperty("url")
    @NotBlank
    private String url = null;

    @JsonProperty("token")
    private String token = null;

    public String getType() {
        return type;
    }

    public ModelInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public ModelInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ModelInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getToken() {
        return token;
    }

    public ModelInfo setToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelInfo {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    token: ").append(toIndentedString(token)).append("\n");
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
        ModelInfo modelInfo = (ModelInfo) o;
        return Objects.equals(this.type, modelInfo.type) && Objects.equals(this.name, modelInfo.name) && Objects.equals(
            this.url, modelInfo.url) && Objects.equals(this.token, modelInfo.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, url, token);
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
