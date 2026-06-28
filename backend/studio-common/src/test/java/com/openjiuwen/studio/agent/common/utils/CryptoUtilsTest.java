/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoUtilsTest {

    @Mock
    private Ciphers ciphers;

    @BeforeEach
    void setUp() {
        new CryptoUtils(ciphers);
    }

    @Test
    void testEncrypt() {
        when(ciphers.encrypt("hello")).thenReturn("encrypted");
        assertEquals("encrypted", CryptoUtils.encrypt("hello"));
    }

    @Test
    void testDecrypt() {
        when(ciphers.decrypt("encrypted")).thenReturn("hello");
        assertEquals("hello", CryptoUtils.decrypt("encrypted"));
    }

    @Test
    void testEncrypt_NullInput() {
        when(ciphers.encrypt(null)).thenReturn(null);
        assertEquals(null, CryptoUtils.encrypt(null));
    }

    @Test
    void testDecrypt_NullInput() {
        when(ciphers.decrypt(null)).thenReturn(null);
        assertEquals(null, CryptoUtils.decrypt(null));
    }
}
