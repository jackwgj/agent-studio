/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent节点转换IR
 *
 */
public class AgentNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 系统prompt模板
     */
    private static final String SYSTEM_PROMPT = "system_prompt";

    /**
     * 供大模型调用的插件列表
     */
    private static final String PLUGINS = "plugins";

    /**
     * 插件id
     */
    private static final String ID = "id";

    /**
     * 允许意图退出
     */
    private static final String ENABLE_INTENT_BREAK = "enable_intent_break";

    /**
     * 跳出插件id
     */
    private static final String BREAK_PLUGIN_IDS = "break_plugin_ids";

    /**
     * 跳出最大迭代轮次
     */
    private static final String MAX_ITERATION = "max_iteration";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        configs.put(IRUtils.adaptKey(ENABLE_INTENT_BREAK),
            nodeConfigs.get(ENABLE_INTENT_BREAK) != null ? nodeConfigs.get(ENABLE_INTENT_BREAK) : false);
        configs.put(IRUtils.adaptKey(BREAK_PLUGIN_IDS),
            nodeConfigs.get(BREAK_PLUGIN_IDS) != null ? nodeConfigs.get(BREAK_PLUGIN_IDS) : new ArrayList<>());

        // 最大迭代次数，默认值为1
        configs.put(IRUtils.adaptKey(MAX_ITERATION),
            nodeConfigs.get(MAX_ITERATION) != null ? nodeConfigs.get(MAX_ITERATION) : 1);
        adaptModel(configs, nodeConfigs);
        configs.put(IRUtils.adaptKey(SYSTEM_PROMPT), nodeConfigs.get(SYSTEM_PROMPT));
        if (nodeConfigs.get(PLUGINS) != null) {
            List<Map<String, Object>> pluginConfigs = new ArrayList<>();
            List<Map<String, Object>> originPlugins = JsonUtils.objectToClass(nodeConfigs.get(PLUGINS));
            if (originPlugins != null) {
                try {
                    IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);
                    for (Map<String, Object> pluginConfig : originPlugins) {
                        pluginConfigs.add(irAdapterService.parsePluginConfig(pluginConfig,
                            RequestContextUtils.getRequestProjectId(), false));
                    }
                } catch (AgentStudioException e) {
                    throw new AgentStudioException(StudioError.GET_TOOL_INFO_FAILED, workflowNodeVo.getName());
                }
            }
            configs.put(PLUGINS, pluginConfigs);
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.AGENT.getIrType();
    }
}
