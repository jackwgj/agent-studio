/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection;


import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;

import okhttp3.Headers;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证处理器
 *
 * @since 2025-08-22
 */
@Service
public interface IRestAuthHandler {

    String ADDRESS = "endpoint";

    /**
     * 根据接口调用的相关信息生成基本的请求
     *
     */
    Headers generateAuthRequest(List<KnowledgeBaseConnectionParam> connectorParamDefinitions);

    default String getHost(List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        for (KnowledgeBaseConnectionParam paramDefinition : connectorParamDefinitions) {
            if (paramDefinition.getCode().equals(ADDRESS)) {
                return paramDefinition.getValue();
            }
        }
        return "";
    }

}
