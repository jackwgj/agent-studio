/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.dto.ControllerExecutionDetail;
import com.openjiuwen.studio.agent.common.dto.run.GetControllerExecutionDetailQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversationsQo;
import com.openjiuwen.studio.agent.runtime.dto.ListControllerConversionsResp;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsQo;
import com.openjiuwen.studio.agent.common.dto.run.ListControllerExecutionsResp;
import com.openjiuwen.studio.agent.runtime.model.debugging.ControllerExecutionBriefModel;
import com.openjiuwen.studio.agent.runtime.model.debugging.ControllerExecutionDetailModel;
import com.openjiuwen.studio.agent.runtime.model.debugging.mapper.CtrlExecutionModelMapper;
import com.openjiuwen.studio.agent.runtime.service.debugging.ControllerDebuggingMgmtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 多智能体会话管理 Service
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ControllerConversationMgmtService implements IControllerConversationMgmtService {
    private final ControllerDebuggingMgmtService controllerDebuggingMgmtService;

    @Override
    public ControllerExecutionDetail getControllerExecutionDetail(String projectId, String agentId, String executionId,
        GetControllerExecutionDetailQo getControllerExecutionDetailQo) {
        try{
            ControllerExecutionDetailModel controllerExecutionDetailModel
                = controllerDebuggingMgmtService.queryCtrlExecutionDetail(agentId, executionId);
            return CtrlExecutionModelMapper.INSTANCE.toCtrlExecDetail(
                controllerExecutionDetailModel);
        }catch (Exception e){
            log.error("getControllerExecutionDetail fail", e);
            throw e;
        }
    }

    @Override
    public ListControllerConversionsResp listControllerConversations(String projectId, String agentId,
        ListControllerConversationsQo listControllerConversationsQo) {
        ListControllerConversionsResp listControllerConversionsResp = new ListControllerConversionsResp();
        listControllerConversionsResp.setCount(0);
        listControllerConversionsResp.setConversations(Collections.emptyList());
        return listControllerConversionsResp;
    }

    @Override
    public ListControllerExecutionsResp listControllerExecutions(String projectId, String agentId,
        String conversationId, ListControllerExecutionsQo listControllerExecutionsQo) {
        List<ControllerExecutionBriefModel> ctrlExecBriefModels = controllerDebuggingMgmtService.queryCtrlExecutions(
            agentId, conversationId, listControllerExecutionsQo);

        ListControllerExecutionsResp listControllerExecutionsResp = new ListControllerExecutionsResp();
        listControllerExecutionsResp.setCount(ctrlExecBriefModels.size());
        listControllerExecutionsResp.setExecutions(CtrlExecutionModelMapper.INSTANCE.toCtrlExecs(ctrlExecBriefModels));

        return listControllerExecutionsResp;
    }
}
