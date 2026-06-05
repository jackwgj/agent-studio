/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is the result of each evaluation cell
 */
@ApiModel(description = "This is the result of each evaluation cell")

@Validated
public class PromptEvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("test_num")
    private Integer testNum = null;

    @JsonProperty("variables")
    @Valid
    private List<VariableVo> variables = null;

    @JsonProperty("expect_result")
    @Length(max = 512)
    private String expectResult = null;

    @JsonProperty("prompt_result_list")
    @Valid
    @NotNull
    private List<PeEvaluationResultVo> promptResultList = new ArrayList<PeEvaluationResultVo>();

    public Integer getTestNum() {
        return testNum;
    }

    public PromptEvaluationResult setTestNum(Integer testNum) {
        this.testNum = testNum;
        return this;
    }

    public List<VariableVo> getVariables() {
        return variables;
    }

    public PromptEvaluationResult setVariables(List<VariableVo> variables) {
        this.variables = variables;
        return this;
    }

    public String getExpectResult() {
        return expectResult;
    }

    public PromptEvaluationResult setExpectResult(String expectResult) {
        this.expectResult = expectResult;
        return this;
    }

    public List<PeEvaluationResultVo> getPromptResultList() {
        return promptResultList;
    }

    public PromptEvaluationResult setPromptResultList(List<PeEvaluationResultVo> promptResultList) {
        this.promptResultList = promptResultList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PromptEvaluationResult {\n");
        sb.append("    testNum: ").append(toIndentedString(testNum)).append("\n");
        sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
        sb.append("    expectResult: ").append(toIndentedString(expectResult)).append("\n");
        sb.append("    promptResultList: ").append(toIndentedString(promptResultList)).append("\n");
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
        PromptEvaluationResult promptEvaluationResult = (PromptEvaluationResult) o;
        return Objects.equals(this.testNum, promptEvaluationResult.testNum) && Objects.equals(this.variables,
            promptEvaluationResult.variables) && Objects.equals(this.expectResult, promptEvaluationResult.expectResult)
            && Objects.equals(this.promptResultList, promptEvaluationResult.promptResultList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testNum, variables, expectResult, promptResultList);
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
