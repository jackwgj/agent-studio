/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;

import java.util.Map;

public class NoAuthAdapter implements AuthAdapter {
    NoAuthAdapter() {
    }

    @Override
    public Map<String, String> requestHeaderHandler(String url, String method, Map<String, String> originHeaders,
        Map<String, String> hds, Object requestBody, ProviderAuth auth) {
        return hds;
    }
}
