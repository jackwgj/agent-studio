/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection;

import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;

import org.springframework.http.HttpEntity;

/**
 * 认证处理器
 *
 * @since 2025-08-22
 */
public interface IRestAuthHandler {

    /**
     * 根据接口调用的相关信息生成基本的请求
     *
     * @param connectorDefinition
     * @return
     */
    HttpEntity<Object> generateAuthRequest(ConnectorDefinition connectorDefinition, BasicRequest basicRequest);

}
