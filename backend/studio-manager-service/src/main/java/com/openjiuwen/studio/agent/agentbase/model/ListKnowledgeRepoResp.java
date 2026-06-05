/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询知识库列表响应实体类
 *
 * @since 2024-04-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListKnowledgeRepoResp {
    @JsonProperty("data_list")
    private List<KnowledgeRepoInfo> dataList;

    private Long total;
}
