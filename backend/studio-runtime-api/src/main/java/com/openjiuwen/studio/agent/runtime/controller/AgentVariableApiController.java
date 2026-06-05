/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.AgentVariableRsp;
import com.openjiuwen.studio.agent.runtime.dto.GetAgentVariableQo;
import com.openjiuwen.studio.agent.runtime.dto.UpdateVariableReq;
import com.openjiuwen.studio.agent.runtime.dto.VariableInfo;
import com.openjiuwen.studio.agent.runtime.service.IAgentVariableService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * AgentVariable controller
 */
@RestController

public class AgentVariableApiController implements AgentVariableApi {
    private static final Logger log = LoggerFactory.getLogger(AgentVariableApiController.class);

    @Autowired
    private IAgentVariableService agentVariableService;

    @Override
    public ResponseEntity<AgentVariableRsp> getAgentVariable(String projectId, String agentId, String conversationId, GetAgentVariableQo getAgentVariableQo) {
        return ResponseModel.success(agentVariableService.getAgentVariable(projectId, agentId, conversationId, getAgentVariableQo));
    }

    @Override
    public ResponseEntity<VariableInfo> updateAgentVariable(String projectId, String agentId, String conversationId, String varId, UpdateVariableReq body) {
        return ResponseModel.success(agentVariableService.updateAgentVariable(projectId, agentId, conversationId, varId, body));
    }
}