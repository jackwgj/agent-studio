/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件节点转换IR
 *
 */
@Slf4j
public class PluginNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 插件节点id
     */
    private static final String ID = "id";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        Map<String, Object> pluginConfig;
        try {
            IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);
            pluginConfig =
                irAdapterService.parsePluginConfig(nodeConfigs, RequestContextUtils.getRequestProjectId(), false);
        } catch (AgentStudioException e) {
            log.error("adapt plugin node failed", e);
            throw new AgentStudioException(StudioError.GET_TOOL_INFO_FAILED, workflowNodeVo.getName());
        }

        Map<String, Object> configs = new HashMap<>(pluginConfig);
        if (nodeConfigs.get(CommonConstant.Http.EXCEPTION_ENABLE) != null) {
            configs.put(IRUtils.adaptKey(CommonConstant.Http.EXCEPTION_ENABLE),
                nodeConfigs.get(CommonConstant.Http.EXCEPTION_ENABLE));
            configs.put(IRUtils.adaptKey(CommonConstant.Http.EXCEPTION_SUPPRESSION),
                nodeConfigs.get(CommonConstant.Http.EXCEPTION_SUPPRESSION));
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.PLUGIN.getIrType();
    }
}
