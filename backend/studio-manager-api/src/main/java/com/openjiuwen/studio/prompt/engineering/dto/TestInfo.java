/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 测试用例相关信息
 */
@ApiModel(description = "测试用例相关信息")

@Validated
public class TestInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String id = null;

    @JsonProperty("question")
    @NotBlank
    @Length(max = 512)
    private String question = null;

    @JsonProperty("standard_answer")
    @NotBlank
    @Length(max = 512)
    private String standardAnswer = null;

    @JsonProperty("set_id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String setId = null;

    public String getId() {
        return id;
    }

    public TestInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getQuestion() {
        return question;
    }

    public TestInfo setQuestion(String question) {
        this.question = question;
        return this;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public TestInfo setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
        return this;
    }

    public String getSetId() {
        return setId;
    }

    public TestInfo setSetId(String setId) {
        this.setId = setId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TestInfo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    question: ").append(toIndentedString(question)).append("\n");
        sb.append("    standardAnswer: ").append(toIndentedString(standardAnswer)).append("\n");
        sb.append("    setId: ").append(toIndentedString(setId)).append("\n");
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
        TestInfo testInfo = (TestInfo) o;
        return Objects.equals(this.id, testInfo.id) && Objects.equals(this.question, testInfo.question)
            && Objects.equals(this.standardAnswer, testInfo.standardAnswer) && Objects.equals(this.setId,
            testInfo.setId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, question, standardAnswer, setId);
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
