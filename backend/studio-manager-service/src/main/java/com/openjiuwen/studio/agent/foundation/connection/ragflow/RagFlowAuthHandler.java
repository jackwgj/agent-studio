/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.ragflow;

import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.foundation.connection.ConnectionParamType;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorParamDefinition;
import com.openjiuwen.studio.agent.foundation.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Optional;

/**
 * RagFlow请求认证处理
 *
 * @since 2025-08-22
 */
public class RagFlowAuthHandler implements IRestAuthHandler {

    public static final String ADDRESS = "endpoint";

    public static final String API_KEY = "APIKey";

    public static final String AUTH_HEADER = "Bearer ";

    @Override
    public HttpEntity<Object> generateAuthRequest(ConnectorDefinition connectorDefinition, BasicRequest basicRequest) {
        String address = null;
        String apiKey = null;

        for (ConnectorParamDefinition paramDefinition : connectorDefinition.getParamDefinitions()) {
            switch (paramDefinition.getCode()) {
                case ADDRESS:
                    address = paramDefinition.getValue();
                    break;
                case API_KEY:
                    if (paramDefinition.getType() == ConnectionParamType.SECRET) {
                        apiKey = CryptoUtils.decrypt(paramDefinition.getValue());
                    } else {
                        apiKey = paramDefinition.getValue();
                    }
                    break;
            }
        }
        basicRequest.setHost(address);
        RequestEntity requestEntity = basicRequest.getRequestEntity();
        HttpHeaders headers = Optional.ofNullable(requestEntity)
            .map(RequestEntity::getHeaders)
            .orElse(new HttpHeaders());
        headers.put(HttpHeaders.AUTHORIZATION, Lists.newArrayList(AUTH_HEADER + apiKey));
        if (basicRequest.getMethod() != HttpMethod.GET) {
            headers.put(HttpHeaders.CONTENT_TYPE, Lists.newArrayList(MediaType.APPLICATION_JSON_VALUE));
        }
        requestEntity.setHeaders(headers);
        return new HttpEntity<>(requestEntity.getBody(), requestEntity.getHeaders());
    }
}
