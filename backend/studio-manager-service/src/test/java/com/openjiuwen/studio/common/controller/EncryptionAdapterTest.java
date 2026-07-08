/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.common.service.service.EncryptionAdapter;
import com.openjiuwen.studio.common.service.service.LegacyCryptoHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptionAdapterTest {
    @InjectMocks
    private EncryptionAdapter encryptionAdapter;

    @Mock
    private LegacyCryptoHandler legacyCryptoHandler;

    private static final String PLAIN_TEXT = "hello_world";

    private static final String CIPHER_TEXT = "E1-{kms-uuid}-xxxxxx";

    @Test
    @DisplayName("加密：输入为空字符串 -> 原样返回")
    void testEncrypt_EmptyString() {
        assertEquals("", encryptionAdapter.encrypt("", "domainId"));
    }

    @Test
    @DisplayName("加密：输入为null -> 原样返回")
    void testEncrypt_Null() {
        assertNull(encryptionAdapter.encrypt(null, "domainId"));
    }

    @Test
    @DisplayName("加密：legacyCryptoHandler正常 -> 返回加密结果")
    void testEncrypt_Success() {
        when(legacyCryptoHandler.encrypt(PLAIN_TEXT)).thenReturn(CIPHER_TEXT);
        assertEquals(CIPHER_TEXT, encryptionAdapter.encrypt(PLAIN_TEXT, "domainId"));
    }

    @Test
    @DisplayName("加密：legacyCryptoHandler抛异常 -> 返回原文")
    void testEncrypt_Exception_ReturnOriginal() {
        when(legacyCryptoHandler.encrypt(anyString())).thenThrow(new RuntimeException("Encrypt error"));
        assertEquals(PLAIN_TEXT, encryptionAdapter.encrypt(PLAIN_TEXT, "domainId"));
    }

    @Test
    @DisplayName("解密：输入为空字符串 -> 原样返回")
    void testDecrypt_EmptyString() {
        when(legacyCryptoHandler.decrypt("")).thenReturn("");
        assertEquals("", encryptionAdapter.decrypt(""));
    }

    @Test
    @DisplayName("解密：输入为null -> 原样返回")
    void testDecrypt_Null() {
        assertNull(encryptionAdapter.decrypt(null));
    }

    @Test
    @DisplayName("解密：legacyCryptoHandler正常 -> 返回解密结果")
    void testDecrypt_Success() {
        when(legacyCryptoHandler.decrypt(CIPHER_TEXT)).thenReturn(PLAIN_TEXT);
        assertEquals(PLAIN_TEXT, encryptionAdapter.decrypt(CIPHER_TEXT));
    }

    @Test
    @DisplayName("解密：legacyCryptoHandler抛异常 -> 返回原文")
    void testDecrypt_Exception_ReturnOriginal() {
        when(legacyCryptoHandler.decrypt(anyString())).thenThrow(new RuntimeException("Decrypt error"));
        assertEquals(CIPHER_TEXT, encryptionAdapter.decrypt(CIPHER_TEXT));
    }
}
