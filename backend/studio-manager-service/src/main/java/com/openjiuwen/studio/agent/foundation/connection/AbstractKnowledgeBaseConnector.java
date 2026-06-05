/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection;

import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;

import org.springframework.http.HttpEntity;

/**
 * 对接第三方知识库的抽象类，提供对接第三方知识库时的一些公共方法
 *
 * @since 2025-08-22
 */
public abstract class AbstractKnowledgeBaseConnector implements IKnowledgeBaseConnector {

    protected IRestAuthHandler authHandler;

    protected ConnectorClient connectorClient;

    public AbstractKnowledgeBaseConnector(ConnectorClient connectorClient, IRestAuthHandler authHandler) {
        this.connectorClient = connectorClient;
        this.authHandler = authHandler;
    }

    /**
     * 生成第三方知识库的认证请求头
     *
     * @param basicRequest
     * @return
     */
    protected HttpEntity<Object> generateAuthRequest(BasicRequest basicRequest,
        ConnectorDefinition connectorDefinition) {
        return authHandler.generateAuthRequest(connectorDefinition, basicRequest);
    }
}
