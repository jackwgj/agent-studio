/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class IamAuthAdapter implements AuthAdapter {
    @Override
    public Map<String, String> requestHeaderHandler(String url, String method, Map<String, String> originHeaders,
        Map<String, String> headers, Object requestBody, ProviderAuth auth) {
        String token = RequestContextUtils.getRequestAuthToken();
        headers.put("X-Auth-Token", token);

        return headers;
    }
}
