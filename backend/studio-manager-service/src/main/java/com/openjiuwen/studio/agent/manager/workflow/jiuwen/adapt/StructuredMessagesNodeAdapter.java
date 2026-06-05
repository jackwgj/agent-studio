/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.apache.commons.lang.SerializationUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述 将dsl转化成异常节点的IR
 *
 */
public class StructuredMessagesNodeAdapter extends AbstractIRNodeAdapter {

    public static String flattenJson(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            return "{}";
        }
        try {
            Map<String, Object> flattenedMap = flatten("", jsonObject);
            return new JSONObject(flattenedMap).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON input", e);
        }
    }

    private static Map<String, Object> flatten(String prefix, JSONObject jsonObject) {
        Map<String, Object> result = new HashMap<>();
        Set<String> keySet = jsonObject.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            String newPrefix = prefix.isEmpty() ? key : prefix + "_" + key;
            if (value instanceof JSONObject) {
                result.putAll(flatten(newPrefix, (JSONObject) value));
            } else if (value instanceof JSONArray) {
                // 数组处理：保留原始结构不展开
                result.put(newPrefix, value);
            } else {
                // 基本类型处理
                result.put(newPrefix, value);
            }
        }
        return result;
    }

    /**
     * 特殊格式转换逻辑：将嵌套JSON对象扁平化后转换为多个输入字段
     */
    private void convertSpecialFields(WorkflowNodeVO nodeVo) {

        WorkflowFieldVOValue value = new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
            .setHint("");
        value.setContent(JSONObject.parseObject((String) nodeVo.getConfigs().get("template")));
        WorkflowFieldVO input = new WorkflowFieldVO();
        input.setName("exception_data");
        input.setType("string");
        input.setDescription("exception_messages");
        input.setRequired(true);
        input.setSource(WorkflowFieldVO.SourceEnum.USER);
        input.setReflection(false);
        input.setValue(value);
        // 获取原始输入字段并提取JSON内容
        nodeVo.setInputs(new ArrayList<>(List.of(input)));
        WorkflowFieldVO originalField = nodeVo.getInputs().get(0);
        JSONObject jsonContent = (JSONObject) originalField.getValue().getContent();

        // 扁平化处理JSON对象
        String flattenedJson = flattenJson(jsonContent);
        JSONObject flattenedData = JSONObject.parseObject(flattenedJson);

        // 创建新的输入字段列表
        List<WorkflowFieldVO> convertedInputs = new ArrayList<>();

        for (String fieldKey : flattenedData.keySet()) {
            // 深度克隆原始字段配置
            WorkflowFieldVO convertedField = (WorkflowFieldVO) SerializationUtils.clone(originalField);

            // 更新字段名和值
            convertedField.setName(fieldKey);
            convertedField.getValue().setContent(flattenedData.get(fieldKey));

            convertedInputs.add(convertedField);
        }

        // 替换节点输入字段
        nodeVo.setInputs(convertedInputs);
    }

    @Override
    public String getNodeType() {
        return NodeType.STRUCTURED_MESSAGES_EXCEPTION.getIrType();
    }

    @Override
    public Map<String, Object> adapt(WorkflowNodeVO workflowNodeVo) {
        // 转化为标准的格式
        convertSpecialFields(workflowNodeVo);
        Map<String, Object> nodeIr = super.adapt(workflowNodeVo);
        return nodeIr;
    }

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        return new HashMap<>();
    }

}
