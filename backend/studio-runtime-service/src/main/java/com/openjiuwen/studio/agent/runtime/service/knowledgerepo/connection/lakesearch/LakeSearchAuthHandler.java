/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.lakesearch;


import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import okhttp3.Headers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

/**
 * LakeSearch请求认证处理
 *
 * @since 2025-08-22
 */
@Service
public class LakeSearchAuthHandler implements IRestAuthHandler {

    public static final String USER_NAME = "user_name";

    public static final String PASSWORD = "password";

    public static final String AUTH_HEADER = "Basic ";


    @Override
    public Headers generateAuthRequest(List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        String userName = null;
        String password = null;

        for (KnowledgeBaseConnectionParam paramDefinition : connectorParamDefinitions) {
            switch (paramDefinition.getCode()) {
                case USER_NAME:
                    userName = paramDefinition.getValue();
                    break;
                case PASSWORD:
                    password = CryptoUtils.decrypt(paramDefinition.getValue());
                    break;
                default:
                    break;
            }
        }
        Headers headers = null;
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {
            // 当MRS开启安全模式时，才会使用认证，如果不开安全模式的话，可以不使用认证请求头
            String authStr = Base64.getEncoder()
                .encodeToString((userName + ":" + password).getBytes(Charset.defaultCharset()));

            headers = Headers.of(HttpHeaders.AUTHORIZATION, AUTH_HEADER + authStr);
        }
        return headers;
    }

}
