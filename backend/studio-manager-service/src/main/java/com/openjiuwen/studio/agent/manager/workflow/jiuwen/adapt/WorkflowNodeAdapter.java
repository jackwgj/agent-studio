/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.WorkflowManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流节点转换IR
 *
 */
public class WorkflowNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 工作流id
     */
    public static final String ID = "id";

    /**
     * 版本id
     */
    public static final String VERSION_ID = "version_id";

    /**
     * ir路径
     */
    private static final String PATH = "path";

    /**
     * 引用
     */
    private static final String REFERENCE = "reference";

    /**
     * 预定义字段
     */
    private static final String PRE_DEFINE_FIELDS = "preDefineFields";

    /**
     * 预定义字段对象
     */
    private static final JSONObject PRE_DEFINE_FIELDS_OBJECT =
        JSONObject.parse("{\"outputs\":[{\"id\":\"responseContent\",\"type\":\"string\"}]}");

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        String workflowId = nodeConfigs.get(ID).toString();
        String versionId = nodeConfigs.get(VERSION_ID).toString();

        // 设置conference参数
        Map<String, String> reference = new HashMap<>();
        reference.put(ID, workflowId);
        WorkflowManagementService wmService = SpringBeanUtils.getBean(WorkflowManagementService.class);
        reference.put(PATH, wmService.getWorkflowObsPath(workflowId, CommonConstant.Workflow.IR, versionId));
        configs.put(REFERENCE, reference);

        // 设置preDefineFields
        configs.put(PRE_DEFINE_FIELDS, PRE_DEFINE_FIELDS_OBJECT);
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.WORKFLOW.getIrType();
    }
}
