/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.List;
import java.util.Map;

/**
 * 工作流节点转换适配器
 *
 */
public interface NodeConverter {

    /**
     * 返回对应的节点类型，根据类型获取对应节点的转换器
     * @param nodeType
     * @return
     */
    Boolean supportNodeType(NodeType nodeType);

    WorkflowNodeVO parseMapData(Map<String, Object> data, Map<String, WorkflowNodeVO> nodeMap);

    WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap);

    /**
     * 转换节点输入参数
     * @return
     */
    List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap);

    /**
     * 转换节点输出参数，dify dsl中无输出参数定义，根据dify的节点返回进行转换
     * @return
     */
    List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data);

    /**
     * 转换节点configs
     * @return
     */
    Map<String, Object> adaptConfigs(Map<String, Object> data);
}
