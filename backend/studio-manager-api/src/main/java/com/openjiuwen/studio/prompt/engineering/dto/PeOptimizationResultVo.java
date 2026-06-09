/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * This is the result of each prompt
 */
@ApiModel(description = "This is the result of each prompt")

@Validated
public class PeOptimizationResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("prompt_id")
    private String promptId = null;

    @JsonProperty("prompt_name")
    @Length(max = 512)
    private String promptName = null;

    @JsonProperty("generate_result")
    @Length(max = 512)
    private String generateResult = null;

    @JsonProperty("eval_failed_reason")
    @Length(max = 255)
    private String evalFailedReason = null;

    public String getId() {
        return id;
    }

    public PeOptimizationResultVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getPromptId() {
        return promptId;
    }

    public PeOptimizationResultVo setPromptId(String promptId) {
        this.promptId = promptId;
        return this;
    }

    public String getPromptName() {
        return promptName;
    }

    public PeOptimizationResultVo setPromptName(String promptName) {
        this.promptName = promptName;
        return this;
    }

    public String getGenerateResult() {
        return generateResult;
    }

    public PeOptimizationResultVo setGenerateResult(String generateResult) {
        this.generateResult = generateResult;
        return this;
    }

    public String getEvalFailedReason() {
        return evalFailedReason;
    }

    public PeOptimizationResultVo setEvalFailedReason(String evalFailedReason) {
        this.evalFailedReason = evalFailedReason;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeOptimizationResultVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    promptId: ").append(toIndentedString(promptId)).append("\n");
        sb.append("    promptName: ").append(toIndentedString(promptName)).append("\n");
        sb.append("    generateResult: ").append(toIndentedString(generateResult)).append("\n");
        sb.append("    evalFailedReason: ").append(toIndentedString(evalFailedReason)).append("\n");
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
        PeOptimizationResultVo peOptimizationResultVo = (PeOptimizationResultVo) o;
        return Objects.equals(this.id, peOptimizationResultVo.id) && Objects.equals(this.promptId,
            peOptimizationResultVo.promptId) && Objects.equals(this.promptName, peOptimizationResultVo.promptName)
            && Objects.equals(this.generateResult, peOptimizationResultVo.generateResult) && Objects.equals(
            this.evalFailedReason, peOptimizationResultVo.evalFailedReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, promptId, promptName, generateResult, evalFailedReason);
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
