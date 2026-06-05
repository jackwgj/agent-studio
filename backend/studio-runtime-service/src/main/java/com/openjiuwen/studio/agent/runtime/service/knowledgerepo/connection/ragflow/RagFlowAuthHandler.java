/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.ragflow;

import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import okhttp3.Headers;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RagFlow请求认证处理
 *
 * @since 2025-08-22
 */
@Service
public class RagFlowAuthHandler implements IRestAuthHandler {

    public static final String API_KEY = "APIKey";

    public static final String AUTH_HEADER = "Bearer ";

    @Override
    public Headers generateAuthRequest(List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        String apiKey = null;

        for (KnowledgeBaseConnectionParam paramDefinition : connectorParamDefinitions) {
            if (paramDefinition.getCode().equals(API_KEY)) {
                apiKey = CryptoUtils.decrypt(paramDefinition.getValue());
            }
        }
        return Headers.of(HttpHeaders.AUTHORIZATION, AUTH_HEADER + apiKey);
    }
}
