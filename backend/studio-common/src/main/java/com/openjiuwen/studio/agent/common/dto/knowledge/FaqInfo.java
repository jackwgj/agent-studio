/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

/**
 * faq信息
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqInfo {
    private String id;

    @JsonProperty("repo_id")
    private String repoId;

    private String question;

    private String answer;

    private String question1;

    private String question2;

    private String question3;

    private String question4;

    private String category;

    private String tags;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("update_time")
    private String updateTime;

    /**
     * FaqInfo转换成KnowledgeFaq
     *
     * @return KnowledgeFaq
     */
    public KnowledgeFaq convertToDto() {
        KnowledgeFaq knowledgeFaq = new KnowledgeFaq().setFaqId(id)
            .setKnowledgeRepoId(repoId)
            .setQuestion(question)
            .setAnswer(answer)
            .setQuestion1(question1)
            .setQuestion2(question2)
            .setQuestion3(question3)
            .setQuestion4(question4)
            .setCategory(category)
            .setTags(tags);
        if (StringUtils.isNotBlank(createTime)) {
            knowledgeFaq.setCreateTime(Long.parseLong(createTime));
        }
        if (StringUtils.isNotBlank(updateTime)) {
            knowledgeFaq.setUpdateTime(Long.parseLong(updateTime));
        }
        return knowledgeFaq;
    }
}
