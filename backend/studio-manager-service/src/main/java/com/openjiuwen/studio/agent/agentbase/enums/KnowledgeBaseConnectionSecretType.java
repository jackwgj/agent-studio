/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.enums;

import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class KnowledgeBaseConnectionSecretType {

    /**
     * 知识库连接信息中敏感数据字段，用于查询时屏蔽为******
     * secret info type in knowledge base connection
     */
    private static final Set<String> SECRET_KEY_LIST = Set.of("apiKey", "token", "APIKey", "password", "AppCode",
        "authorization");

    public static void anonymizeParameters(KnowledgeBaseConnectionParam param) {
        if (StringUtils.isNotEmpty(param.getCode()) && SECRET_KEY_LIST.contains(param.getCode())) {
            param.setValue("******");
        }
    }
}
