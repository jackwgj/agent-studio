/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.dto.AgentVariableRsp;
import com.openjiuwen.studio.agent.runtime.dto.GetAgentVariableQo;
import com.openjiuwen.studio.agent.runtime.dto.UpdateVariableReq;
import com.openjiuwen.studio.agent.runtime.dto.VariableInfo;


/**
 * AgentVariable service
 */

public interface IAgentVariableService {

    /**
     * getAgentVariable
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param conversationId conversationId
     * @param getAgentVariableQo getAgentVariableQo
     */
    AgentVariableRsp getAgentVariable(String projectId, String agentId, String conversationId, GetAgentVariableQo getAgentVariableQo) ;

    /**
     * updateAgentVariable
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param conversationId conversationId
     * @param varId varId
     * @param body body
     */
    VariableInfo updateAgentVariable(String projectId, String agentId, String conversationId, String varId, UpdateVariableReq body) ;
}