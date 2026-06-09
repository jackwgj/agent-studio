/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * 自动追问请求体
 */
@ApiModel(description = "自动追问请求体")
@Validated
public class AdditionalQuestionsReq {
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

    public String getName() {
        return name;
    }

    public AdditionalQuestionsReq setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdditionalQuestionsReq setDescription(String description) {
        this.description = description;
        return this;
    }

    public AdditionalQuestionsReq setEnable(Boolean enable) {
        this.enable = enable;
        return this;
    }

    public Boolean isEnable() {
        return enable;
    }

    public String getPrompt() {
        return prompt;
    }

    public AdditionalQuestionsReq setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public AdditionalQuestionsReq setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdditionalQuestionsReq {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    enable: ").append(toIndentedString(enable)).append("\n");
        sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
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
        AdditionalQuestionsReq additionalQuestionsReq = (AdditionalQuestionsReq) o;
        return Objects.equals(this.name, additionalQuestionsReq.name) && Objects.equals(this.description,
            additionalQuestionsReq.description) && Objects.equals(this.enable, additionalQuestionsReq.enable)
            && Objects.equals(this.prompt, additionalQuestionsReq.prompt) && Objects.equals(this.versionId,
            additionalQuestionsReq.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, enable, prompt, versionId);
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
