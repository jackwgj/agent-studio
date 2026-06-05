/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.ControllerExecutionDetail;
import com.openjiuwen.studio.agent.common.dto.run.GetControllerExecutionDetailQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversationsQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversionsResp;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsQo;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsResp;
import com.openjiuwen.studio.agent.runtime.service.IControllerConversationMgmtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * ControllerConversationMgmt controller
 */
@RestController

public class ControllerConversationMgmtApiController implements ControllerConversationMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(ControllerConversationMgmtApiController.class);

    @Autowired
    private IControllerConversationMgmtService controllerConversationMgmtService;

    @Override
    public ResponseEntity<ControllerExecutionDetail> getControllerExecutionDetail(String projectId, String agentId, String executionId, GetControllerExecutionDetailQo getControllerExecutionDetailQo) {
        return ResponseModel.success(controllerConversationMgmtService.getControllerExecutionDetail(projectId, agentId, executionId, getControllerExecutionDetailQo));
    }

    @Override
    public ResponseEntity<ListControllerConversionsResp> listControllerConversations(String projectId, String agentId, ListControllerConversationsQo listControllerConversationsQo) {
        return ResponseModel.success(controllerConversationMgmtService.listControllerConversations(projectId, agentId, listControllerConversationsQo));
    }

    @Override
    public ResponseEntity<ListControllerExecutionsResp> listControllerExecutions(String projectId, String agentId, String conversationId, ListControllerExecutionsQo listControllerExecutionsQo) {
        return ResponseModel.success(controllerConversationMgmtService.listControllerExecutions(projectId, agentId, conversationId, listControllerExecutionsQo));
    }
}