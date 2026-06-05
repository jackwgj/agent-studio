/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.enums.BoolQueryNodeType;
import com.openjiuwen.studio.agent.manager.enums.BoolQueryOperatorType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 布尔查询节点类，用于表示高级标签过滤的树形结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoolQueryNode {
    /**
     * 节点类型: "GROUP" 或 "RULE"
     */
    @JsonProperty("type")
    private BoolQueryNodeType type;

    /**
     * 操作符: "AND", "OR", "NOT"
     */
    @JsonProperty("operator")
    private BoolQueryOperatorType operator;

    /**
     * 具体的标签值，仅当type为RULE时有效
     */
    @JsonProperty("value")
    private String value;

    /**
     * 子节点列表，仅当type为GROUP时有效
     */
    @JsonProperty("children")
    private List<BoolQueryNode> children;
}
