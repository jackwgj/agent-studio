/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.dto.TriggerConfig;
import com.openjiuwen.studio.agent.common.dto.TriggerParam;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.AgentIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 功能描述
 *
 */
@Slf4j
@Service
public class HookTriggerService {

    @Autowired
    private AgentRuntimeService agentRuntimeService;

    @Autowired
    private AgentIrCacheService agentIrCacheService;

    public SseEmitter runHookTrigger(String projectId, String agentId, String triggerId, Map<String, String> body) {
        AgentIrWrapper agentIrWrapper = agentIrCacheService.getLatestAgentIr(agentId);
        AgentIrMetadata metadata = agentIrWrapper.getMetadata();
        List<TriggerConfig> triggerList = metadata.getTriggerConfigs();
        for (TriggerConfig triggerConfig : triggerList) {
            if (triggerConfig.getTriggerId().equals(triggerId)) {
                String prompt = triggerConfig.getPrompt();
                List<TriggerParam> triggerParamList = triggerConfig.getParams();
                for (TriggerParam triggerParam : triggerParamList) {
                    if (body.get(triggerParam.getName()) != null) {
                        String paramStr = "{{" + triggerParam.getName() + "}}";
                        prompt = prompt.replace(paramStr, body.get(triggerParam.getName()));
                    }
                }
                AgentExecuteParams agentExecuteParams = AgentExecuteParams.builder()
                    .projectId(projectId)
                    .agentId(agentId)
                    .query(prompt)
                    .conversationId(UUID.randomUUID().toString())
                    .startTime(System.currentTimeMillis())
                    .build();
                return agentRuntimeService.runAgentStream(agentExecuteParams);
            }
        }
        log.error("Trigger does not exist! projectId = {}, agentId = {}, triggerId = {}", projectId, agentId,
            triggerId);
        throw new AgentStudioException(StudioError.AGENT_TRIGGER_EXIST, triggerId);
    }
}
