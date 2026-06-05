/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.koosearch;

import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.foundation.connection.ConnectionParamType;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorParamDefinition;
import com.openjiuwen.studio.agent.foundation.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Optional;

/**
 * KooSearch请求认证处理
 *
 * @since 2025-12-1
 */
public class KooSearchAuthHandler implements IRestAuthHandler {

    public static final String ADDRESS = "endpoint";

    public static final String PROJECT_ID = "project_id";

    public static final String APPLICATION_ID = "application_id";

    public static final String APPCODE = "AppCode";

    @Override
    public HttpEntity<Object> generateAuthRequest(ConnectorDefinition connectorDefinition, BasicRequest basicRequest) {
        String address = null;
        String projectId = null;
        String applicationId = null;
        String appCode = null;

        for (ConnectorParamDefinition paramDefinition : connectorDefinition.getParamDefinitions()) {
            switch (paramDefinition.getCode()) {
                case ADDRESS:
                    address = paramDefinition.getValue();
                    break;
                case PROJECT_ID:
                    projectId = paramDefinition.getValue();
                    break;
                case APPLICATION_ID:
                    applicationId = paramDefinition.getValue();
                    break;
                case APPCODE:
                    if (paramDefinition.getType() == ConnectionParamType.SECRET) {
                        appCode = CryptoUtils.decrypt(paramDefinition.getValue());
                    } else {
                        appCode = paramDefinition.getValue();
                    }
                    break;
            }
        }
        basicRequest.setHost(address);
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(applicationId) || ObjectUtils.isEmpty(
            basicRequest.getRequestEntity())) {
            return null;
        }
        basicRequest.getRequestEntity()
            .setPathVariables(Map.of("project_id", projectId, "application_id", applicationId));
        RequestEntity requestEntity = basicRequest.getRequestEntity();
        HttpHeaders headers = Optional.ofNullable(requestEntity)
            .map(RequestEntity::getHeaders)
            .orElse(new HttpHeaders());
        if (StringUtils.isNotEmpty(APPCODE)) {
            headers.put("X-Apig-AppCode", Lists.newArrayList(appCode));
        }
        if (basicRequest.getMethod() != HttpMethod.GET) {
            headers.put(HttpHeaders.CONTENT_TYPE, Lists.newArrayList(MediaType.APPLICATION_JSON_VALUE));
        }
        requestEntity.setHeaders(headers);
        return new HttpEntity<>(requestEntity.getBody(), requestEntity.getHeaders());
    }
}
