/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AnswerNodeConverter extends AbstractSDSLNodeConverter {

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.ANSWER.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.MESSAGE.getType());
        List<WorkflowFieldVO> needConvertFields = new ArrayList<>();
        workflowNodeVO.setConfigs(adaptConfigs(data, needConvertFields, workflowNodeMap));
        workflowNodeVO.setInputs(needConvertFields);
        workflowNodeVO.setOutputs(adaptOutputs(data));
        return workflowNodeVO;
    }

    public Map<String, Object> adaptConfigs(Map<String, Object> data, List<WorkflowFieldVO> needConvertFields, Map<String, WorkflowNodeVO> workflowNodeMap) {
        Map<String, Object> configsMap = new HashMap<>();
        configsMap.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, false);
        configsMap.put("isStructMessage", false);
        boolean isInLoop = (boolean) data.getOrDefault(CommonConstant.DIFY.IS_IN_LOOP, false) || (boolean) data.getOrDefault(CommonConstant.DIFY.IS_IN_ITERATION, false);
        configsMap.put(CommonConstant.DIFY.IS_IN_LOOP, isInLoop);
        String answer = data.get("answer").toString();
        Map<String, Set<List<String>>> idNameMap = new HashMap<>();
        configsMap.put("template", extractAndReplace(answer, idNameMap));
        // 转换输入参数
        needConvertFields.addAll(convertToFieldVOs(idNameMap, workflowNodeMap));
        return configsMap;
    }

    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {
        return List.of(new WorkflowFieldVO().setName("result")
                .setType("string").setDescription("消息输出")
                .setRequired(false).setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue().setContent("").setType(WorkflowFieldVOValue.TypeEnum.GENERATED)));
    }
}
