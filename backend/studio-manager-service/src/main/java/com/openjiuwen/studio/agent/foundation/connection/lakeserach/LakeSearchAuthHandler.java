/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.lakeserach;

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

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

/**
 * LakeSearch请求认证处理
 *
 * @since 2025-08-22
 */
public class LakeSearchAuthHandler implements IRestAuthHandler {

    public static final String ADDRESS = "endpoint";

    public static final String USER_NAME = "user_name";

    public static final String PASSWORD = "password";

    public static final String AUTH_HEADER = "Basic ";

    @Override
    public HttpEntity<Object> generateAuthRequest(ConnectorDefinition connectorDefinition, BasicRequest basicRequest) {
        String address = null;
        String userName = null;
        String password = null;

        for (ConnectorParamDefinition paramDefinition : connectorDefinition.getParamDefinitions()) {
            switch (paramDefinition.getCode()) {
                case ADDRESS:
                    address = paramDefinition.getValue();
                    break;
                case USER_NAME:
                    userName = paramDefinition.getValue();
                    break;
                case PASSWORD:
                    if (paramDefinition.getType() == ConnectionParamType.SECRET) {
                        password = CryptoUtils.decrypt(paramDefinition.getValue());
                    } else {
                        password = paramDefinition.getValue();
                    }
                    break;
            }
        }
        basicRequest.setHost(address);
        RequestEntity requestEntity = basicRequest.getRequestEntity();
        HttpHeaders headers = Optional.ofNullable(requestEntity)
            .map(RequestEntity::getHeaders)
            .orElse(new HttpHeaders());
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            // 当MRS开启安全模式时，才会使用认证，如果不开安全模式的话，可以不使用认证请求头
            String authStr = Base64.getEncoder()
                .encodeToString((userName + ":" + password).getBytes(Charset.defaultCharset()));
            headers.put(HttpHeaders.AUTHORIZATION, Lists.newArrayList(AUTH_HEADER + authStr));
        }
        if (basicRequest.getMethod() != HttpMethod.GET) {
            headers.put(HttpHeaders.CONTENT_TYPE, Lists.newArrayList(MediaType.APPLICATION_JSON_VALUE));
        }
        requestEntity.setHeaders(headers);
        return new HttpEntity<>(requestEntity.getBody(), requestEntity.getHeaders());
    }
}
