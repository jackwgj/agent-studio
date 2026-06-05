/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.ApiKeyAuthInfo;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class AuthAdapterFactoryTest {
    @Test
    public void getAuthAdapterTest() {
        AuthAdapter auth = AuthAdapterFactory.getAuthAdapter(new NotAuthInfo());
        assertEquals(NoAuthAdapter.class, auth.getClass());

        auth = AuthAdapterFactory.getAuthAdapter(new ApiKeyAuthInfo());
        assertEquals(ApiKeyAuthAdapter.class, auth.getClass());

        ProviderAuth test = new ProviderAuth() {
            @Override
            public String type() {
                return "test";
            }

            @Override
            public void validCheck() {

            }

            @Override
            public String convertToEncryptStr() {
                return "";
            }

            @Override
            public String authInfoTemplate() {
                return "";
            }

            @Override
            public String convertToMaskStr(boolean decrypt) {
                return "";
            }
        };

        try {
            AuthAdapterFactory.getAuthAdapter(test);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.AUTH_TYPE_UNSUPPORTED, e.getErrorCode());
        }
    }

    @Test
    public void noAuthTest() {
        AuthAdapter auth = AuthAdapterFactory.getAuthAdapter(new NotAuthInfo());
        assertEquals(NoAuthAdapter.class, auth.getClass());
        auth.requestHeaderHandler("", "", new HashMap<>(), new HashMap<>(), "", new NotAuthInfo());
    }

    @Test
    public void apiKeyTest() {
        ApiKeyAuthInfo apiKeyAuthInfo = new ApiKeyAuthInfo();
        apiKeyAuthInfo.setApiKey("test");

        AuthAdapter auth = AuthAdapterFactory.getAuthAdapter(apiKeyAuthInfo);

        new CryptoUtils(new Ciphers(null, null));

        try (MockedStatic<CryptoUtils> mockedSccCryptoUtils = Mockito.mockStatic(CryptoUtils.class)) {
            mockedSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("test");
            mockedSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("test");

            Map<String, String> headers = auth.requestHeaderHandler("", "", new HashMap<>(), new HashMap<>(), "",
                apiKeyAuthInfo);
            assertEquals("Bearer test", headers.get("Authorization"));
        }
    }
}
