/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSegmentRule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * 知识文件分层规则实体类
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSegmentRuleEntity {
    private String id;

    @JsonProperty("project_id")
    private String projectId;

    private String rule;

    private String creator;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("created_on")
    private Long createdOn;

    @JsonProperty("updated_on")
    private Long updatedOn;

    /**
     * 数据库实体类转换成DTO
     *
     * @return KnowledgeSegmentRule
     */
    public KnowledgeSegmentRule convertToDto() {
        KnowledgeSegmentRule segmentRule = new KnowledgeSegmentRule();
        segmentRule.setId(id);
        segmentRule.setProjectId(projectId);
        segmentRule.setRuleRegexs(Arrays.asList(rule.split(CommonConstant.ZERO_SPACE, -1)));
        segmentRule.setCreator(creator);
        segmentRule.setCreatorId(creatorId);
        segmentRule.setCreateTime(createdOn);
        segmentRule.setUpdateTime(updatedOn);
        return segmentRule;
    }
}
