/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;

import java.util.Map;

public interface AuthAdapter {
    /**
     * 模型认证请求处理
     *
     * @param url 请求地址
     * @param method 请求方法
     * @param originHeaders 原始请求Header参数
     * @param headers 调用模型目标header
     * @param requestBody 请求提
     * @param auth 认证配置
     * @return 处理后的模型认证配置
     */
    Map<String, String> requestHeaderHandler(String url, String method, Map<String, String> originHeaders,
        Map<String, String> headers, Object requestBody, ProviderAuth auth);
}
