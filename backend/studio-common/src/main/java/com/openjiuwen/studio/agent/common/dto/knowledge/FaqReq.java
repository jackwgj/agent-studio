/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建FAQ请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqReq {
    @JsonProperty("question")
    private String question;

    @JsonProperty("question1")
    private String question1;

    @JsonProperty("question2")
    private String question2;

    @JsonProperty("question3")
    private String question3;

    @JsonProperty("question4")
    private String question4;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("repo_id")
    private String repoId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("category")
    private String category;

    private String tags;

    public FaqReq(KnowledgeFaq knowledgeFaq) {
        this.id = knowledgeFaq.getFaqId();
        this.repoId = knowledgeFaq.getKnowledgeRepoId();
        this.question = knowledgeFaq.getQuestion();
        this.answer = knowledgeFaq.getAnswer();
        this.question1 = knowledgeFaq.getQuestion1();
        this.question2 = knowledgeFaq.getQuestion2();
        this.question3 = knowledgeFaq.getQuestion3();
        this.question4 = knowledgeFaq.getQuestion4();
        this.category = knowledgeFaq.getCategory();
        this.tags = knowledgeFaq.getTags();
    }
}
