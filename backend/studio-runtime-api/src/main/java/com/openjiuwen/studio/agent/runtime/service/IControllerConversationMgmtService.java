/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.dto.ControllerExecutionDetail;
import com.openjiuwen.studio.agent.common.dto.run.GetControllerExecutionDetailQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversationsQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversionsResp;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsQo;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsResp;


/**
 * ControllerConversationMgmt service
 */

public interface IControllerConversationMgmtService {

    /**
     * getControllerExecutionDetail
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param executionId executionId
     * @param getControllerExecutionDetailQo getControllerExecutionDetailQo
     */
    ControllerExecutionDetail getControllerExecutionDetail(String projectId, String agentId, String executionId, GetControllerExecutionDetailQo getControllerExecutionDetailQo) ;

    /**
     * listControllerConversations
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param listControllerConversationsQo listControllerConversationsQo
     */
    ListControllerConversionsResp listControllerConversations(String projectId, String agentId, ListControllerConversationsQo listControllerConversationsQo) ;

    /**
     * listControllerExecutions
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param conversationId conversationId
     * @param listControllerExecutionsQo listControllerExecutionsQo
     */
    ListControllerExecutionsResp listControllerExecutions(String projectId, String agentId, String conversationId, ListControllerExecutionsQo listControllerExecutionsQo) ;
}