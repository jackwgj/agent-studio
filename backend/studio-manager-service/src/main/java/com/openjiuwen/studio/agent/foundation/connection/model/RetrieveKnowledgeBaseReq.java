/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.model;

import com.openjiuwen.studio.agent.foundation.connection.constants.QueryModeEnum;

import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 知识检索的请求体
 *
 * @since 2025-08-22
 */
@Data
public class RetrieveKnowledgeBaseReq {

    private List<knowledgeBaseRetrieval> knowledgeBaseRetrievals;

    /**
     * 知识检索的输入
     */
    private String query;

    /**
     * 检索模式
     */
    private QueryModeEnum queryMode;

    private List<KnowledgeBaseExtraParam> extraParams;

    private Integer topK;

    private Float searchThreshold;

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseRetrievals.stream().map(knowledgeBaseRetrieval::getKnowledgeBaseId).toList();
    }

    public List<String> getAllTags() {
        return knowledgeBaseRetrievals.stream().filter(Objects::nonNull).flatMap(knowledgeBaseRetrieval -> {
            List<String> tagNames = knowledgeBaseRetrieval.getTagName();
            return tagNames == null ? Stream.empty() : tagNames.stream();
        }).filter(Objects::nonNull).collect(Collectors.toSet()).stream().sorted().collect(Collectors.toList());
    }
}
