/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 循环节点转换IR
 *
 */
public class LoopNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 循环模式，支持array和number
     */
    private static final String LOOP_TYPE = "loop_type";

    /**
     * 循环体内节点id
     */
    private static final String LOOP_BODY = "loop_body";

    /**
     * 次数循环
     */
    private static final String NUM_LOOP_VAR = "num_loop_var";

    /**
     * 次数循环
     */
    private static final String NUM_LOOP = "numLoop";

    /**
     * 次数循环最大
     */
    private static final int NUM_LOOP_MAX = 1000;

    /**
     * 循环中止条件
     */
    private static final String BREAK_CONDITION = "break_condition";

    /**
     * 意图动作配置节点id前缀
     */
    private static final String INTENT_DETECTION_CONTAINER_ID_PREFIX = "node_intent_container_";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();

        validateNumLoopVar(nodeConfigs, workflowNodeVo.getInputs());
        Map<String, Object> configs = new HashMap<>();
        configs.put(IRUtils.adaptKey(LOOP_TYPE), nodeConfigs.get(LOOP_TYPE));
        if (nodeConfigs.containsKey(LOOP_BODY)) {
            List<String> loopBodyIds = JsonUtils.objectToClass(nodeConfigs.get(LOOP_BODY));
            if (loopBodyIds != null) {
                // IR中没有意图动作配置节点，需要从loopBody中去掉意图动作配置节点id
                List<String> filteredIds = loopBodyIds.stream().filter(id -> !id.contains(
                    INTENT_DETECTION_CONTAINER_ID_PREFIX)).collect(Collectors.toList());
                configs.put(IRUtils.adaptKey(LOOP_BODY), filteredIds);
            }
        }
        if (nodeConfigs.get(BREAK_CONDITION) != null) {
            WorkflowBranchConfigVO breakCondition =
                JsonUtils.objectToClassType(nodeConfigs.get(BREAK_CONDITION), WorkflowBranchConfigVO.class);
            if (breakCondition != null) {
                configs.put(IRUtils.adaptKey(BREAK_CONDITION), new com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt.BranchNodeAdapter().adaptExpression(breakCondition));
            }
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.LOOP.getIrType();
    }

    private void validateNumLoopVar(Map<String, Object> nodeConfigs, List<WorkflowFieldVO> inputs) {
        if (nodeConfigs.get(LOOP_TYPE) == null
            || !StringUtils.equals(nodeConfigs.get(LOOP_TYPE).toString(), NUM_LOOP)) {
            return;
        }
        for (WorkflowFieldVO input : inputs) {
            if (StringUtils.equals(input.getName(), NUM_LOOP_VAR)) {
                WorkflowFieldVOValue value = input.getValue();

                // 需要校验输入类型
                if (!StringUtils.equals(value.getType().toString(), WorkflowFieldVOValue.TypeEnum.LITERAL.toString())) {
                    break;
                }
                Integer content = (Integer) input.getValue().getContent();
                if (content == null || content < 1 || content > NUM_LOOP_MAX) {
                    throw new AgentStudioException(StudioError.WORKFLOW_LOOP_EXCEED_RANGE);
                }
            }
        }
    }
}
