/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.Map;

/**
 * 工作流对象转换IR
 *
 */
public interface Adapter {
    /**
     * 工作流节点config端转换
     *
     * @param workflowNodeVo 工作流节点信息
     * @return 节点配置IR
     */
    Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo);

    /**
     * 获取工作流节点类型
     *
     * @return 节点类型
     */
    String getNodeType();

    /**
     * 工作流节点IR接口
     *
     * @param workflowNodeVo 工作流节点信息
     * @return 节点IR
     */
    Map<String, Object> adapt(WorkflowNodeVO workflowNodeVo);

    /**
     * 引用转换
     *
     * @param nodeId 节点id
     * @param varName 变量名
     * @param fieldType 变量类型
     * @return 引用值
     */
    String adaptValue(String nodeId, String varName, String fieldType);

    /**
     * 适配模型参数
     *
     * @param jiuwenConfigs 九问configs
     * @param eiConfigs ei configs
     */
    void adaptModel(Map<String, Object> jiuwenConfigs, Map<String, Object> eiConfigs);
}
