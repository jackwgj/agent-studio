/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.AgentBaseRagConnection;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AgentBaseRagConnectionProvider extends KnowledgeSourceConnectionProvider {

    private static final String ENDPOINT = "endpoint";

    protected AgentBaseRagConnectionProvider(KnowledgeBaseConnectionMapper knowledgeConnectionMapper) {
        super(knowledgeConnectionMapper);
    }

    @Override
    public AgentBaseRagConnection getKnowledgeSourceConnection(String connectionId) {
        try {
            // 此处应查询数据库获取知识源连接信息
            KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = queryKnowledgeConnectionFromDb(connectionId);
            return convertToAgentBaseRag(knowledgeBaseConnectionEntity);
        } catch (Exception exception) {
            log.error("Fail to get KnowledgeBase Connection Info", exception.getMessage());
            throw new AgentBaseException(ErrorCode.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO, exception);
        }
    }

    private AgentBaseRagConnection convertToAgentBaseRag(KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        List<KnowledgeBaseConnectionParam> knowledgeBaseConnectionParams = knowledgeBaseConnectionEntity.getParams();
        AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();

        for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
            if (knowledgeBaseConnectionParam.getCode().equals(ENDPOINT)) {
                agentBaseRagConnection.setEndpoint(knowledgeBaseConnectionParam.getValue());
            }
        }
        agentBaseRagConnection.isValid();
        return agentBaseRagConnection;
    }
}
