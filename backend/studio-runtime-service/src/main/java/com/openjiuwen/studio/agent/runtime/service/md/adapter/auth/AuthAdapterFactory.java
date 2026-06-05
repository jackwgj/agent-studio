/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.dto.md.ApiKeyAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.CustomApiKeyAuthInfo;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuthAdapterFactory {
    private static final Map<String, AuthAdapter> AUTH_ADAPTER;

    static {
        AUTH_ADAPTER = new HashMap<>(3);

        AUTH_ADAPTER.put(new NotAuthInfo().type(), new NoAuthAdapter());
        AUTH_ADAPTER.put(new ApiKeyAuthInfo().type(), new ApiKeyAuthAdapter());
        AUTH_ADAPTER.put(new CustomApiKeyAuthInfo().type(), new CustomApiKeyAuthAdapter());
    }

    public static AuthAdapter getAuthAdapter(ProviderAuth auth) {
        AuthAdapter adapter = AUTH_ADAPTER.get(auth.type());
        if (adapter == null) {
            log.error("Auth type is not support. {}", auth.type());
            throw new AgentStudioException(StudioError.AUTH_TYPE_UNSUPPORTED);
        }
        return adapter;
    }
}
