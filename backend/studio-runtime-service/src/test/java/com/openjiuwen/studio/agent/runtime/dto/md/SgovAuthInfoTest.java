/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto.md;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SgovAuthInfoTest {
    private MockedStatic<CryptoUtils> mgSccCryptoUtils;

    private SgovAuthInfo auth;

    @BeforeEach
    void setUp() throws Exception {
        auth = new SgovAuthInfo("{\"appId\":\"app\",\"credential\":\"cre\",\"sgovurl\":\"url\",\"scenarioUuid\":\"scenarioUuid\"}");
        mgSccCryptoUtils = Mockito.mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);
        mgSccCryptoUtils.when(() -> CryptoUtils.decrypt(Mockito.anyString())).thenReturn("cre");
        mgSccCryptoUtils.when(() -> CryptoUtils.encrypt(Mockito.anyString())).thenReturn("cre");
    }

    @AfterEach
    void tearDown() throws Exception {
        mgSccCryptoUtils.close();
    }

    @Test
    public void sgovAuthTest() {
        Assertions.assertEquals("SGOV", auth.type());
        try {
            new SgovAuthInfo().validCheck();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_AUTH_INFO_INVALID, e.getErrorCode());
        }

        auth.setSgovUrl("url");
        auth.setCredential("cre");
        auth.setAppId("app");
        auth.validCheck();
        Assertions.assertEquals(
            "{\"appId\":\"\",\"credential\":\"\",\"sgovUrl\":\"\",\"userId\":\"\",\"scenarioUuid\":\"\"}",
            auth.authInfoTemplate());
        Assertions.assertEquals(
            "{\"appId\":\"app\",\"credential\":\"c***e\",\"sgovUrl\":\"url\",\"userId\":\"\",\"scenarioUuid\":\"scenarioUuid\"}",
            auth.convertToMaskStr(true));
        Assertions.assertEquals(
            "{\"appId\":\"app\",\"credential\":\"c***e\",\"sgovUrl\":\"url\",\"userId\":\"\",\"scenarioUuid\":\"scenarioUuid\"}",
            auth.convertToMaskStr(false));
        Assertions.assertEquals(
            "{\"appId\":\"app\",\"credential\":\"cre\",\"sgovUrl\":\"url\",\"userId\":\"\",\"scenarioUuid\":\"scenarioUuid\"}",
            auth.convertToEncryptStr());
    }
}
