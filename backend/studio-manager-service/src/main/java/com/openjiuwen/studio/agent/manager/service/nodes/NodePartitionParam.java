/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.nodes;

import com.openjiuwen.studio.agent.manager.dto.WorkflowEdgeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 工作流运行参数
 *
 */
@Builder
@Data
public class NodePartitionParam {
    private List<WorkflowNodeVO> nodes;

    private List<WorkflowEdgeVO> edges;

    private String elseBranchId;

    private List<String> exceptionBranchIds;

    private String loopNodeId;

    private WorkflowNodeVO startNode;

    private WorkflowNodeVO modelNode;

    private WorkflowNodeVO endNode;

    private WorkflowNodeVO exceptionNode;
}
