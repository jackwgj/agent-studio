/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.bo;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 工作流并行终点节点信息，包含被访问的标记位（标记算法依赖）以及进入的边
 *
 * @since 2025.2.27
 */
@Data
public class WorkflowParallelEndNode {
    /**
     * 被访问的标记位
     */
    private Set<Integer> flags = new HashSet<>();

    /**
     * 节点进入的边
     */
    private Set<String> edgeIds = new HashSet<>();

    /**
     * 节点Id
     */
    private String nodeId;
}
