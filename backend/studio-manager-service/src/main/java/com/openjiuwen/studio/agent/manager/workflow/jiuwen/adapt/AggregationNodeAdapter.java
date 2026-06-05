/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.GroupConfig;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 变量聚合节点转换IR
 *
 */
public class AggregationNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 聚合节点模式字段
     */
    private static final String MODE = "mode";

    /**
     * 聚合节点分组信息
     */
    private static final String GROUPS = "groups";

    /**
     * 模式当前支持第一个非空返回
     */
    private static final String FIRST_NON_NULL = "first-non-null";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();

        // 模式默认为第一个非空聚合模式
        configs.put(MODE, nodeConfigs.get(MODE) != null ? nodeConfigs.get(MODE) : FIRST_NON_NULL);

        // 参数配置需进行排序
        configs.put(GROUPS, new HashMap<>());
        if (nodeConfigs.get(GROUPS) != null) {
            Map<String, List<String>> groups = new HashMap<>();
            Map<String, List<Object>> groupConfigs = JsonUtils.objectToClass(nodeConfigs.get(GROUPS));
            if (groupConfigs != null && !groupConfigs.isEmpty()) {
                for (Map.Entry<String, List<Object>> groupConfig : groupConfigs.entrySet()) {
                    List<GroupConfig> result = groupConfig.getValue().stream().map(m -> {
                        TypeReference<GroupConfig> type = new TypeReference<>() {};
                        return JsonUtils.json2Obj(JsonUtils.toJson(m), type);
                    }).toList();
                    groups.put(groupConfig.getKey(),
                        result.stream()
                            .sorted(Comparator.comparing(GroupConfig::getIndex))
                            .map(GroupConfig::getName)
                            .toList());
                }
                configs.put(GROUPS, groups);
            }
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.AGGREGATION.getIrType();
    }
}
