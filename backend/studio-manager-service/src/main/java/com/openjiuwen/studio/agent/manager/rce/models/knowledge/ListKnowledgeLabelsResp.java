/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeTagInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库标签列表查询响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListKnowledgeLabelsResp {
    private Integer total;

    @JsonProperty("label_list")
    private List<KnowledgeTagInfo> labelList;
}
