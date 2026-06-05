/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规划节点转换IR
 *
 */
public class TaskFlowNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * prompt模板
     */
    private static final String PROMPT = "prompt";

    /**
     * 规划模式
     */
    private static final String PLANNER = "planner";

    /**
     * 插件
     */
    private static final String PLUGINS = "plugins";

    /**
     * id
     */
    private static final String ID = "id";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {

        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        adaptModel(configs, nodeConfigs);
        configs.put(PROMPT, nodeConfigs.get(PROMPT));
        configs.put(PLANNER, nodeConfigs.get(PLANNER));
        if (nodeConfigs.get(PLUGINS) != null) {
            List<Map<String, Object>> pluginConfigs = new ArrayList<>();
            List<Map<String, Object>> originPlugins = JsonUtils.objectToClass(nodeConfigs.get(PLUGINS));
            if (originPlugins != null) {
                IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);
                for (Map<String, Object> pluginConfig : originPlugins) {
                    pluginConfigs.add(irAdapterService.parsePluginConfig(pluginConfig,
                        RequestContextUtils.getRequestProjectId(), false));
                }
            }
            configs.put(PLUGINS, pluginConfigs);
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.TASK_FLOW.getIrType();
    }
}
