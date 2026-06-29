/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.dto.md.CustomApiKeyAuthInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

class CustomApiKeyAuthAdapterTest {
    @Test
    public void testCustomApiKeyAuthAdapter() {
        CustomApiKeyAuthInfo auth = new CustomApiKeyAuthInfo();
        MockedStatic<CryptoUtils> mgSccCryptoUtils = Mockito.mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);
        try (mgSccCryptoUtils) {
            mgSccCryptoUtils.when(() -> CryptoUtils.decrypt("tt")).thenReturn("tt");
            mgSccCryptoUtils.when(() -> CryptoUtils.decrypt("uu")).thenReturn("uu");
            mgSccCryptoUtils.when(() -> CryptoUtils.decrypt("123")).thenReturn("123");
            mgSccCryptoUtils.when(() -> CryptoUtils.decrypt("vv")).thenReturn("vv");
            mgSccCryptoUtils.when(() -> CryptoUtils.decrypt("cust-not-exist")).thenReturn("cust-not-exist");

            Map<String, String> originHeaders = new HashMap<>();
            originHeaders.put("cust-demo", "demo");
            originHeaders.put("cust-xxx", "xxx");
            originHeaders.put("token", "cust-token");
            originHeaders.put("userid", "cust-userid");

            CustomApiKeyAuthAdapter adapter = new CustomApiKeyAuthAdapter();

            Map<String, String> ans = adapter.requestHeaderHandler("url", "method", originHeaders, new HashMap<>(),
                "body", auth);
            Assertions.assertEquals("{}", ans.toString());

            Map<String, String> authData = new HashMap<>();
            authData.put("cust-token", "tt");
            authData.put("cust-userid", "uu");
            authData.put("cust-demo", "uu");
            authData.put("cust-not-exist", "cust-not-exist");
            authData.put("token", "123");
            authData.put("tt", "vv");

            auth.setAuthInfo(authData);

            ans = adapter.requestHeaderHandler("url", "method", originHeaders, new HashMap<>(), "body", auth);
            Assertions.assertEquals("{tt=vv, cust-demo=uu, userid=uu, token=123, cust-not-exist=cust-not-exist}",
                ans.toString());

            ans = adapter.requestHeaderHandler("url", "method", new HashMap<>(), new HashMap<>(), "body", auth);
            Assertions.assertEquals("{tt=vv, cust-demo=uu, userid=uu, token=123, cust-not-exist=cust-not-exist}",
                ans.toString());

            adapter = new CustomApiKeyAuthAdapter();
            ReflectionTestUtils.setField(adapter, "customHeaderReplace", false);
            ans = adapter.requestHeaderHandler("url", "method", originHeaders, new HashMap<>(), "body", auth);
            Assertions.assertEquals(
                "{tt=vv, cust-demo=uu, cust-token=tt, cust-userid=uu, cust-not-exist=cust-not-exist, token=123}",
                ans.toString());
        }
    }
}
