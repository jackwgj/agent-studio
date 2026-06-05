/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.dto.md.CustomApiKeyAuthInfo;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

class CustomApiKeyAuthInfoTest {
    private MockedStatic<CryptoUtils> mgSccCryptoUtils;

    private CustomApiKeyAuthInfo auth;

    @BeforeEach
    void setUp() throws Exception {
        auth = new CustomApiKeyAuthInfo();
        Map<String, String> authInfo = new HashMap<>();
        authInfo.put("key", "value");
        auth.setAuthInfo(authInfo);
        mgSccCryptoUtils = Mockito.mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);
        mgSccCryptoUtils.when(() -> CryptoUtils.decrypt(Mockito.anyString())).thenReturn("pwd");
        mgSccCryptoUtils.when(() -> CryptoUtils.encrypt(Mockito.anyString())).thenReturn("pwd");
    }

    @AfterEach
    void tearDown() throws Exception {
        mgSccCryptoUtils.close();
    }

    @Test
    public void customIAMAuthTest() {
        Assertions.assertEquals("CUSTOM_APIKEY", auth.type());
        auth.validCheck();

        auth = new CustomApiKeyAuthInfo("{\"test\":\"123456\"}");
        auth.validCheck();
        Assertions.assertEquals("{\"\":\"\"}", auth.authInfoTemplate());
        Assertions.assertEquals("{\"test\":\"p***d\"}", auth.convertToMaskStr(true));
        Assertions.assertEquals("{\"test\":\"1***6\"}", auth.convertToMaskStr(false));
        Assertions.assertEquals("{\"test\":\"pwd\"}", auth.convertToEncryptStr());

        Map<String, String> authInfo = JsonUtils.decode("{\"test\":\"123456\"}",
            new TypeReference<Map<String, String>>() { });

        auth = new CustomApiKeyAuthInfo(authInfo);
        auth.validCheck();
        Assertions.assertEquals("{\"\":\"\"}", auth.authInfoTemplate());
        Assertions.assertEquals("{\"test\":\"p***d\"}", auth.convertToMaskStr(true));
        Assertions.assertEquals("{\"test\":\"1***6\"}", auth.convertToMaskStr(false));
        Assertions.assertEquals("{\"test\":\"pwd\"}", auth.convertToEncryptStr());
    }
}
