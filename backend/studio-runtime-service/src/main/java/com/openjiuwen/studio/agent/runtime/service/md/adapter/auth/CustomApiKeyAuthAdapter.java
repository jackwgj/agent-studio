/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.dto.md.CustomApiKeyAuthInfo;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class CustomApiKeyAuthAdapter implements AuthAdapter {
    private final boolean customHeaderReplace = "true".equals(
        System.getenv().getOrDefault("custom_apikey_header_replace", "true"));

    private Map<String, String> allHeadersReplace(Map<String, String> authConfig) {
        Map<String, String> headers = new HashMap<>(authConfig.size());
        for (Map.Entry<String, String> entry : authConfig.entrySet()) {
            headers.put(entry.getKey(), CryptoUtils.decrypt(entry.getValue()));
        }
        return headers;
    }

    private Map<String, String> customHeadersReplace(Map<String, String> authConfig) {
        Map<String, String> headers = new HashMap<>(authConfig.size());
        for (Map.Entry<String, String> entry : authConfig.entrySet()) {
            String lowerKey = entry.getKey().toLowerCase(Locale.ROOT);
            String realKey = "cust-token".equals(lowerKey) || "cust-userid".equals(lowerKey)
                ? lowerKey.substring(5)
                : lowerKey;
            headers.put(realKey, CryptoUtils.decrypt(entry.getValue()));
        }
        return headers;
    }

    @Override
    public Map<String, String> requestHeaderHandler(String url, String method, Map<String, String> originHeaders,
        Map<String, String> headers, Object requestBody, ProviderAuth auth) {
        CustomApiKeyAuthInfo apiKeyAuth = (CustomApiKeyAuthInfo) auth;
        if (apiKeyAuth.getAuthInfo() == null) {
            return headers;
        }
        Map<String, String> authHeaders = customHeaderReplace ? customHeadersReplace(
            apiKeyAuth.getAuthInfo()) : allHeadersReplace(apiKeyAuth.getAuthInfo());
        headers.putAll(authHeaders);
        return headers;
    }
}
