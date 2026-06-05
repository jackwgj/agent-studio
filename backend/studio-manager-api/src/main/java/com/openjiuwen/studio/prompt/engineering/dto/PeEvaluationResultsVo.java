/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is the information of the Evaluation Task Results
 */
@ApiModel(description = "This is the information of the Evaluation Task Results")

@Validated
public class PeEvaluationResultsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("total")
    private Integer total = null;

    @JsonProperty("count")
    private Integer count = null;

    @JsonProperty("result_list")
    @Valid
    @NotNull
    private List<PromptEvaluationResult> resultList = new ArrayList<PromptEvaluationResult>();

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("method")
    private String method = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("optimal_prompt_id")
    private String optimalPromptId = null;

    @JsonProperty("prompts")
    @Valid
    private List<PePromptResultVo> prompts = null;

    public Integer getTotal() {
        return total;
    }

    public PeEvaluationResultsVo setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public PeEvaluationResultsVo setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<PromptEvaluationResult> getResultList() {
        return resultList;
    }

    public PeEvaluationResultsVo setResultList(List<PromptEvaluationResult> resultList) {
        this.resultList = resultList;
        return this;
    }

    public String getName() {
        return name;
    }

    public PeEvaluationResultsVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public PeEvaluationResultsVo setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PeEvaluationResultsVo setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getOptimalPromptId() {
        return optimalPromptId;
    }

    public PeEvaluationResultsVo setOptimalPromptId(String optimalPromptId) {
        this.optimalPromptId = optimalPromptId;
        return this;
    }

    public List<PePromptResultVo> getPrompts() {
        return prompts;
    }

    public PeEvaluationResultsVo setPrompts(List<PePromptResultVo> prompts) {
        this.prompts = prompts;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeEvaluationResultsVo {\n");
        sb.append("    total: ").append(toIndentedString(total)).append("\n");
        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    resultList: ").append(toIndentedString(resultList)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    optimalPromptId: ").append(toIndentedString(optimalPromptId)).append("\n");
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
        PeEvaluationResultsVo peEvaluationResultsVo = (PeEvaluationResultsVo) o;
        return Objects.equals(this.total, peEvaluationResultsVo.total) && Objects.equals(this.count,
            peEvaluationResultsVo.count) && Objects.equals(this.resultList, peEvaluationResultsVo.resultList)
            && Objects.equals(this.name, peEvaluationResultsVo.name) && Objects.equals(this.method,
            peEvaluationResultsVo.method) && Objects.equals(this.status, peEvaluationResultsVo.status)
            && Objects.equals(this.optimalPromptId, peEvaluationResultsVo.optimalPromptId) && Objects.equals(
            this.prompts, peEvaluationResultsVo.prompts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, count, resultList, name, method, status, optimalPromptId, prompts);
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
