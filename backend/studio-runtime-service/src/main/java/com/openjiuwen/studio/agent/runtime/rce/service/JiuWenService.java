/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrReq;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrRsp;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JiuWenService {
    private final JiuWenClient jiuWenClient;

    public JiuWenService(JiuWenClient jiuWenClient) {
        this.jiuWenClient = jiuWenClient;
    }

    public JiuWenDeleteIrRsp deleteConversationIr(String conversationId, String irPath) {
        try {
            String authToken = RequestContextUtils.getRequestAuthToken();
            JiuWenDeleteIrReq jiuWenDeleteIrReq;
            if (!StringUtils.isEmpty(irPath)) {
                jiuWenDeleteIrReq = JiuWenDeleteIrReq.builder().conversationId(conversationId).irPath(irPath).build();
            } else {
                jiuWenDeleteIrReq = JiuWenDeleteIrReq.builder().conversationId(conversationId).build();
            }
            JiuWenDeleteIrRsp rsp = jiuWenClient.deleteIr(authToken, jiuWenDeleteIrReq);
            log.info("Delete jiuWen conversation IR code [{}] message [{}]", rsp.getCode(), rsp.getMessage());
            return rsp;
        } catch (Exception exception) {
            log.error("Fail to delete conversation IR from jiuWen service: [{}]", exception.getMessage());
            throw new AgentStudioException(StudioError.JIU_WEN_SERVICE_EXCEPTION);
        }
    }

    public JiuWenDeleteIrRsp deleteConversationIr(String conversationId) {
        return this.deleteConversationIr(conversationId, null);
    }
}
