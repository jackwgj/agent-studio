/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.common.service.model.kms.ErrorDetail;
import com.openjiuwen.studio.common.service.model.kms.KmsDecryptResp;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;
import com.openjiuwen.studio.common.service.service.LegacyCryptoHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EncryptionAdapter 单元测试 (适配无自身兜底、强阻断的最新安全架构)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptionAdapterTest {
    @InjectMocks
    private EncryptionAdapter encryptionAdapter;

    @Mock
    private LegacyCryptoHandler legacyCryptoHandler;

    private MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic;

    private static final String TEST_DOMAIN_ID = "test";

    private static final String PLAIN_TEXT = "hello_world";

    private static final String KMS_CIPHER_TEXT = "E1-{kms-uuid}-xxxxxx";

    private static final String LEGACY_CIPHER_TEXT = "scc-cipher-text";

    @BeforeEach
    void setUp() {
        requestContextUtilsMockedStatic = Mockito.mockStatic(RequestContextUtils.class);
    }

    @AfterEach
    void tearDown() {
        requestContextUtilsMockedStatic.close();
    }

    // ==================== 加密测试 (Encrypt) ====================

    @Test
    @DisplayName("加密：输入为空 -> 原样返回")
    void testEncrypt_NullOrEmpty() {
        assertEquals("", encryptionAdapter.encrypt("", TEST_DOMAIN_ID));
        assertEquals(null, encryptionAdapter.encrypt(null, TEST_DOMAIN_ID));
    }

    @Test
    @DisplayName("解密：输入为空 -> 原样返回")
    void testDecrypt_NullOrEmpty() {
        ReflectionTestUtils.setField(encryptionAdapter, "kmsSwitchEnable", true);
        assertEquals("", encryptionAdapter.decrypt(""));
        assertEquals(null, encryptionAdapter.decrypt(null));
    }

    @Test
    @DisplayName("解密：拥有DomainId且格式合法 -> 交由公共方法处理并成功")
    void testDecrypt_WithDomainId_CallKmsService() {
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(TEST_DOMAIN_ID);
        ReflectionTestUtils.setField(encryptionAdapter, "kmsSwitchEnable", true);
        KmsDecryptResp resp = new KmsDecryptResp();
        resp.setPlainText(PLAIN_TEXT);
    }

    @Test
    @DisplayName("解密兜底：旧数据格式且无DomainId -> 直接交由kmsDecryptUtil公共方法兜底")
    void testDecrypt_LegacyData_NoDomainId_FallbackToScc() {
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(null);
        ReflectionTestUtils.setField(encryptionAdapter, "kmsSwitchEnable", true);
    }

    @Test
    @DisplayName("解密：KMS返回错误且是老数据 -> 交由kmsDecryptUtil公共方法兜底")
    void testDecrypt_KmsError_ButOldData() {
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(TEST_DOMAIN_ID);
        ReflectionTestUtils.setField(encryptionAdapter, "kmsSwitchEnable", true);
        KmsDecryptResp resp = new KmsDecryptResp();
        resp.setError(ErrorDetail.builder().errorMsg("key deleted").build());
        resp.setOldDataFlag(true);
    }

    @Test
    @DisplayName("解密兜底：KmsService抛异常 -> 返回原文")
    void testDecrypt_Exception_ReturnOriginal() {
        ReflectionTestUtils.setField(encryptionAdapter, "isHcs", true);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(TEST_DOMAIN_ID);
        when(legacyCryptoHandler.decrypt(anyString())).thenReturn("E1-{kms-uuid}-xxxxxx");
        String result = encryptionAdapter.decrypt(KMS_CIPHER_TEXT);

        assertEquals(KMS_CIPHER_TEXT, result);
    }

    @Test
    @DisplayName("解密兜底：KmsService抛异常 -> 返回原文")
    void testDecrypt_Exception() {
        ReflectionTestUtils.setField(encryptionAdapter, "isHcs", true);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(TEST_DOMAIN_ID);

        when(legacyCryptoHandler.decrypt(anyString())).thenThrow(new RuntimeException("Decrypt error"));
        String result = encryptionAdapter.decrypt(KMS_CIPHER_TEXT);

        assertEquals(KMS_CIPHER_TEXT, result);
    }

    @Test
    @DisplayName("加密兜底：KmsService抛异常 -> 返回原文")
    void testEncrypt_Exception_ReturnOriginal() {
        ReflectionTestUtils.setField(encryptionAdapter, "isHcs", true);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(TEST_DOMAIN_ID);
    }

    @Test
    @DisplayName("解密兜底：KmsService抛异常 -> 返回原文")
    void testEncrypt_Exception() {
        ReflectionTestUtils.setField(encryptionAdapter, "isHcs", true);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(TEST_DOMAIN_ID);

        when(legacyCryptoHandler.encrypt(anyString())).thenThrow(new RuntimeException("Decrypt error"));
        String result = encryptionAdapter.encrypt(PLAIN_TEXT, TEST_DOMAIN_ID);
        assertEquals(PLAIN_TEXT, result);
    }
}
