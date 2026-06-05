/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

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
