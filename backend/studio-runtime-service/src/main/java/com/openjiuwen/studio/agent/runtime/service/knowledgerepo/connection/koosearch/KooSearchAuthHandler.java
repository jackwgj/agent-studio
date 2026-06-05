/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.koosearch;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.IRestAuthHandler;

import okhttp3.Headers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * KooSearch请求认证处理
 *
 * @since 2025-12-1
 */
@Service
public class KooSearchAuthHandler implements IRestAuthHandler {

    public static final String APPCODE = "AppCode";

    @Override
    public Headers generateAuthRequest(List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        String appCode = null;
        for (KnowledgeBaseConnectionParam paramDefinition : connectorParamDefinitions) {
            if (paramDefinition.getCode().equals(APPCODE)) {
                appCode = CryptoUtils.decrypt(paramDefinition.getValue());
            }
        }
        Headers headers = null;
        if (StringUtils.isNotEmpty(APPCODE)) {
            headers = Headers.of("X-Apig-AppCode", appCode);
        }
        return headers;
    }

}
