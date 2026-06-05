/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.general;


import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.IRestAuthHandler;

import okhttp3.Headers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeneralAuthHandler implements IRestAuthHandler {


    public static final String APIKEY = "apiKey";

    public static final String AUTH_HEADER = "Bearer ";

    @Override
    public Headers generateAuthRequest(List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        String apiKey = null;
        for (KnowledgeBaseConnectionParam paramDefinition : connectorParamDefinitions) {
            if (paramDefinition.getCode().equals(APIKEY)) {
                apiKey = CryptoUtils.decrypt(paramDefinition.getValue());
            }
        }
        Headers headers = null;
        if (StringUtils.isNotEmpty(apiKey)) {
            headers = Headers.of(HttpHeaders.AUTHORIZATION, AUTH_HEADER + apiKey);
        }
        return headers;
    }

}
