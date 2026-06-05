/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用于查询知识库列表的请求消息体
 *
 * @since 2025-12-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListKnowledgeBaseParamsForCustom {

    private String name;

    private List<String> knowledgeBaseIds;

    private Integer offset;

    private Integer limit;

}
