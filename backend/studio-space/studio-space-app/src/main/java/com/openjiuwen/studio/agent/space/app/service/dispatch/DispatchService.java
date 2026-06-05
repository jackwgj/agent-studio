/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.dispatch;

import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderDispatchVo;
import com.openjiuwen.studio.agent.space.app.task.DispatchChatTask;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 任务调度实现
 */
@Slf4j
@Service
public class DispatchService {
    @Resource
    private DispatchChatTask dispatchChatTask;

    /**
     * 调度任务实现
     *
     * @param AgentBuilderDispatchVo 分发任务入参
     */
    public void dispatchTask(AgentBuilderDispatchVo AgentBuilderDispatchVo) {
        checkParams(AgentBuilderDispatchVo);
        dispatchChatTask.createChatTask(AgentBuilderDispatchVo);
    }

    /**
     * 参数校验
     *
     * @param AgentBuilderDispatchVo 校验入参，权限检验在人接接口处
     */
    private void checkParams(AgentBuilderDispatchVo AgentBuilderDispatchVo) {
        if (StringUtils.isEmpty(AgentBuilderDispatchVo.getTaskId())) {
            log.error("[DispatchService] taskId is empty");
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR, "taskId is empty");
        }
    }
}
