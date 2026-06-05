/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * FAQ问答对
 */

@Validated
public class KnowledgeFaq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("faq_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String faqId = null;

    @JsonProperty("knowledge_repo_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String knowledgeRepoId = null;

    @JsonProperty("question")
    @NotBlank
    @Length(min = 1, max = 1000)
    private String question = null;

    @JsonProperty("answer")
    @NotBlank
    @Length(min = 1, max = 10000)
    private String answer = null;

    @JsonProperty("question1")
    @Length(max = 1000)
    private String question1 = null;

    @JsonProperty("question2")
    @Length(max = 1000)
    private String question2 = null;

    @JsonProperty("question3")
    @Length(max = 1000)
    private String question3 = null;

    @JsonProperty("question4")
    @Length(max = 1000)
    private String question4 = null;

    @JsonProperty("create_time")
    private Long createTime = null;

    @JsonProperty("update_time")
    private Long updateTime = null;

    @JsonProperty("category")
    @Length(max = 255)
    private String category = null;

    @JsonProperty("tags")
    @Length(max = 4096)
    private String tags = null;

    public String getFaqId() {
        return faqId;
    }

    public KnowledgeFaq setFaqId(String faqId) {
        this.faqId = faqId;
        return this;
    }

    public String getKnowledgeRepoId() {
        return knowledgeRepoId;
    }

    public KnowledgeFaq setKnowledgeRepoId(String knowledgeRepoId) {
        this.knowledgeRepoId = knowledgeRepoId;
        return this;
    }

    public String getQuestion() {
        return question;
    }

    public KnowledgeFaq setQuestion(String question) {
        this.question = question;
        return this;
    }

    public String getAnswer() {
        return answer;
    }

    public KnowledgeFaq setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    public String getQuestion1() {
        return question1;
    }

    public KnowledgeFaq setQuestion1(String question1) {
        this.question1 = question1;
        return this;
    }

    public String getQuestion2() {
        return question2;
    }

    public KnowledgeFaq setQuestion2(String question2) {
        this.question2 = question2;
        return this;
    }

    public String getQuestion3() {
        return question3;
    }

    public KnowledgeFaq setQuestion3(String question3) {
        this.question3 = question3;
        return this;
    }

    public String getQuestion4() {
        return question4;
    }

    public KnowledgeFaq setQuestion4(String question4) {
        this.question4 = question4;
        return this;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public KnowledgeFaq setCreateTime(Long createTime) {
        this.createTime = createTime;
        return this;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public KnowledgeFaq setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public KnowledgeFaq setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public KnowledgeFaq setTags(String tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KnowledgeFaq {\n");

        sb.append("    faqId: ").append(toIndentedString(faqId)).append("\n");
        sb.append("    knowledgeRepoId: ").append(toIndentedString(knowledgeRepoId)).append("\n");
        sb.append("    question: ").append(toIndentedString(question)).append("\n");
        sb.append("    answer: ").append(toIndentedString(answer)).append("\n");
        sb.append("    question1: ").append(toIndentedString(question1)).append("\n");
        sb.append("    question2: ").append(toIndentedString(question2)).append("\n");
        sb.append("    question3: ").append(toIndentedString(question3)).append("\n");
        sb.append("    question4: ").append(toIndentedString(question4)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
        KnowledgeFaq knowledgeFaq = (KnowledgeFaq) o;
        return Objects.equals(this.faqId, knowledgeFaq.faqId) && Objects.equals(this.knowledgeRepoId,
            knowledgeFaq.knowledgeRepoId) && Objects.equals(this.question, knowledgeFaq.question) && Objects.equals(
            this.answer, knowledgeFaq.answer) && Objects.equals(this.question1, knowledgeFaq.question1)
            && Objects.equals(this.question2, knowledgeFaq.question2) && Objects.equals(this.question3,
            knowledgeFaq.question3) && Objects.equals(this.question4, knowledgeFaq.question4) && Objects.equals(
            this.createTime, knowledgeFaq.createTime) && Objects.equals(this.updateTime, knowledgeFaq.updateTime)
            && Objects.equals(this.category, knowledgeFaq.category) && Objects.equals(this.tags, knowledgeFaq.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faqId, knowledgeRepoId, question, answer, question1, question2, question3, question4,
            createTime, updateTime, category, tags);
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
