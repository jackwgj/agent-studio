/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.utils.JacksonUtils;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalRetrieveResultInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.KnowledgeBaseTagInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.connection.model.RetrieveKnowledgeBaseReq;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对接第三方知识库的入口
 *
 * @since 2025-08-22
 */
@Slf4j
@Service
public class KnowledgeBaseConnectorContext {

    private ConnectorClient connectorClient;

    public KnowledgeBaseConnectorContext(ConnectorClient connectorClient) {
        this.connectorClient = connectorClient;
    }

    /**
     * 查询知识库列表
     *
     * @param name
     * @param offset
     * @param limit
     * @return
     */
    public PageResult<ExternalKnowledgeBaseInfo> listKnowledgeBase(String name, Integer offset, Integer limit,
        ConnectorDefinition connectorDefinition, String token) {
        try {
            AbstractKnowledgeBaseConnector connector = ConnectorFactory.getKnowledgeBaseConnectorInstance(
                connectorDefinition, connectorClient);
            return connector.listKnowledgeBase(connectorDefinition, name, offset, limit);
        } catch (RuntimeException ex) {
            log.error("List Knowledge bases exception{},{}", ex.getMessage(), ex);
            throw new AgentBaseException(ErrorCode.QUERY_THIRD_PARTY_KNOWLEDGE_BASES_ERROR);
        }
    }

    /**
     * 知识检索
     *
     * @param request
     * @param offset
     * @param limit
     * @return
     */
    public PageResult<ExternalRetrieveResultInfo> retrieveKnowledgeBase(RetrieveKnowledgeBaseReq request,
        Integer offset, Integer limit, ConnectorDefinition connectorDefinition, String token) {
        log.info("Query Knowledge base, request: {}", JacksonUtils.toJson(request));
        try {
            AbstractKnowledgeBaseConnector connector = ConnectorFactory.getKnowledgeBaseConnectorInstance(
                connectorDefinition, connectorClient);
            return connector.retrieveKnowledgeBase(connectorDefinition, request, offset, limit);
        } catch (RuntimeException ex) {
            log.error("Query Knowledge base exception{},{}", ex.getMessage(), ex);
            throw new AgentBaseException(ErrorCode.QUERY_THIRD_PARTY_KNOWLEDGE_BASES_ERROR);
        }

    }

    /**
     * 查询某个知识库下的所有标签
     *
     * @param knowledgeBaseId
     * @return
     */
    public List<KnowledgeBaseTagInfo> listKnowledgeBaseTags(String knowledgeBaseId,
        ConnectorDefinition connectorDefinition) {
        try {
            AbstractKnowledgeBaseConnector connector = ConnectorFactory.getKnowledgeBaseConnectorInstance(
                connectorDefinition, connectorClient);
            return connector.listKnowledgeBaseTags(connectorDefinition, knowledgeBaseId);
        } catch (RuntimeException ex) {
            log.error("List Knowledge base tags exception{},{}", ex.getMessage(), ex);
            throw new AgentBaseException(ErrorCode.QUERY_THIRD_PARTY_KNOWLEDGE_BASES_TAGS_ERROR);
        }
    }

}
