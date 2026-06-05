/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.general;

import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.connection.ConnectionParamType;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorParamDefinition;
import com.openjiuwen.studio.agent.foundation.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Slf4j
public class GeneralAuthHandler implements IRestAuthHandler {

    public static final String ADDRESS = "endpoint";

    public static final String APIKEY = "apiKey";

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
                case APIKEY:
                    if (paramDefinition.getType() == ConnectionParamType.SECRET) {
                        apiKey = CryptoUtils.decrypt(paramDefinition.getValue());
                    } else {
                        apiKey = paramDefinition.getValue();
                    }
                    break;
            }
        }

        basicRequest.setHost(address);
        if (ObjectUtils.isEmpty(basicRequest.getRequestEntity())) {
            log.error("can not connnect third party knowledgebase, because the request params is null");
            throw new AgentBaseException(ErrorCode.CALL_EXTERNAL_KNOWLEDGE_ERROR);
        }
        RequestEntity requestEntity = basicRequest.getRequestEntity();
        HttpHeaders headers = Optional.ofNullable(requestEntity)
            .map(RequestEntity::getHeaders)
            .orElse(new HttpHeaders());
        if (StringUtils.isNotEmpty(apiKey)) {
            headers.put(HttpHeaders.AUTHORIZATION, Lists.newArrayList(AUTH_HEADER + apiKey));
        }
        if (basicRequest.getMethod() != HttpMethod.GET) {
            headers.put(HttpHeaders.CONTENT_TYPE, Lists.newArrayList(MediaType.APPLICATION_JSON_VALUE));
        }
        requestEntity.setHeaders(headers);
        return new HttpEntity<>(requestEntity.getBody(), requestEntity.getHeaders());
    }
}
