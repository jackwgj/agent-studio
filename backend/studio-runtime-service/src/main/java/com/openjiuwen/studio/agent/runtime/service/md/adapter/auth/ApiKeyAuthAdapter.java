/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.ApiKeyAuthInfo;

import java.util.Map;

public class ApiKeyAuthAdapter implements AuthAdapter {
    ApiKeyAuthAdapter() {
    }

    @Override
    public Map<String, String> requestHeaderHandler(String url, String method, Map<String, String> originHeaders,
        Map<String, String> headers, Object requestBody, ProviderAuth auth) {
        String apiKey = CryptoUtils.decrypt(((ApiKeyAuthInfo) auth).getApiKey());
        headers.put("Authorization", "Bearer " + apiKey);
        return headers;
    }
}
