/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.bo;

import com.openjiuwen.studio.agent.manager.dto.WorkflowEdgeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流节点信息，用于图解析，包含链接了哪些边以及自身的属性
 *
 * @since 2025.2.25
 */
@Data
public class WorkflowNodeInfo {
    /**
     * 节点出去的边
     */
    private List<WorkflowEdgeVO> edgesOut = new ArrayList<>();

    /**
     * 节点进入的边
     */
    private List<WorkflowEdgeVO> edgesIn = new ArrayList<>();

    /**
     * 节点信息
     */
    private WorkflowNodeVO node;

    /**
     * 是否为分支节点上的锚点
     */
    private boolean isBranch;
}
