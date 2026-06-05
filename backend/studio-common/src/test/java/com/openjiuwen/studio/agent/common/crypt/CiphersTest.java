/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.common.crypt.Cipher;
import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.crypt.NoOpCipher;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class CiphersTest {
    private Ciphers ciphers;

    @BeforeEach
    public void setUp() {
        ciphers = new Ciphers(null, null);
    }

    @Test
    public void testConstructor_WithNullCiphers() {
        Ciphers c = new Ciphers(null, null);
        assertNotNull(c);
    }

    @Test
    public void testConstructor_WithEmptyCiphers() {
        Ciphers c = new Ciphers(Collections.emptyList(), null);
        assertNotNull(c);
    }

    @Test
    public void testConstructor_WithDefaultCipherName() {
        Ciphers c = new Ciphers(null, "NO_OP_CIPHER");
        assertNotNull(c);
    }

    @Test
    public void testConstructor_With_Duplicated_CipherList() {
        NoOpCipher noOpCipher = new NoOpCipher();
        List<Cipher> cipherList = List.of(noOpCipher);
        assertThrows(IllegalArgumentException.class, () -> new Ciphers(cipherList, "NO_OP_CIPHER"));
    }

    @Test
    public void testEncrypt_BlankPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("   ", new byte[] {1, 2, 3, 4});
        assertEquals("   ", result);
    }

    @Test
    public void testEncrypt_EmptyPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("", new byte[] {1, 2, 3, 4});
        assertEquals("", result);
    }

    @Test
    public void testEncrypt_NullPlainText_ReturnsNull() {
        String result = ciphers.encrypt(null, new byte[] {1, 2, 3, 4});
        assertNull(result);
    }

    @Test
    public void testEncrypt_WithDefaultCipher() {
        String plainText = "test message";
        byte[] vector = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        String result = ciphers.encrypt(plainText, vector);
        assertNotNull(result);
    }

    @Test
    public void testEncrypt_WithSpecificCipherName() {
        String plainText = "test message";
        byte[] vector = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        String result = ciphers.encrypt("NO_OP_CIPHER", plainText, vector);
        assertNotNull(result);
    }

    @Test
    public void testEncrypt_SpecificCipher_BlankPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("NO_OP_CIPHER", "   ", new byte[] {1, 2, 3, 4});
        assertEquals("   ", result);
    }

    @Test
    public void testEncrypt_SpecificCipher_EmptyPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("NO_OP_CIPHER", "", new byte[] {1, 2, 3, 4});
        assertEquals("", result);
    }

    @Test
    public void testEncrypt_UnsupportedCipher_ThrowsException() {
        String plainText = "test message";
        byte[] vector = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        assertThrows(AgentStudioException.class, () -> ciphers.encrypt("UNSUPPORTED_CIPHER", plainText, vector));
    }

    @Test
    public void testDecrypt_BlankCipherText_ReturnsBlank() {
        String result = ciphers.decrypt("   ", new byte[] {1, 2, 3, 4});
        assertEquals("   ", result);
    }

    @Test
    public void testDecrypt_EmptyCipherText_ReturnsEmpty() {
        String result = ciphers.decrypt("", new byte[] {1, 2, 3, 4});
        assertEquals("", result);
    }

    @Test
    public void testDecrypt_NullCipherText_ReturnsNull() {
        String result = ciphers.decrypt((String) null, new byte[] {1, 2, 3, 4});
        assertNull(result);
    }

    @Test
    public void testDecrypt_WithDefaultCipher() {
        String plainText = "test message";
        byte[] vector = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        String encrypted = ciphers.encrypt(plainText, vector);
        String decrypted = ciphers.decrypt(encrypted, vector);
        assertNotNull(decrypted);
    }

    @Test
    public void testDecrypt_WithVectorString() {
        String plainText = "test message";
        byte[] vector = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        String encrypted = ciphers.encrypt(plainText, vector);
        String vectorStr = java.util.Base64.getEncoder().encodeToString(vector);
        String decrypted = ciphers.decrypt(encrypted, vectorStr);
        assertNotNull(decrypted);
    }

    @Test
    public void testDecrypt_BlankCipherTextWithStringVector_ReturnsEmpty() {
        String result = ciphers.decrypt("", "AQIDBA==");
        assertEquals("", result);
    }

    @Test
    public void testGenIV_UnsupportedCipher_ThrowsException() {
        assertThrows(AgentStudioException.class, () -> ciphers.genIV("UNSUPPORTED_CIPHER"));
    }

    @Test
    public void testGenIV_Success() {
        byte[] iv = ciphers.genIV("NO_OP_CIPHER");
        assertNotNull(iv);
    }

    @Test
    public void testEncrypt_NoVector_BlankPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("   ");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testEncrypt_NoVector_EmptyPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testEncrypt_NoVector_NullPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt((String) null);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testEncrypt_NoVector_WithDefaultCipher() {
        String plainText = "test message";
        String result = ciphers.encrypt(plainText);
        assertNotNull(result);
    }

    @Test
    public void testEncrypt_NoVector_SpecificCipher() {
        String plainText = "test message";
        String result = ciphers.encrypt("NO_OP_CIPHER", plainText);
        assertNotNull(result);
    }

    @Test
    public void testEncrypt_NoVector_SpecificCipher_BlankPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("NO_OP_CIPHER", "   ");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testEncrypt_NoVector_SpecificCipher_EmptyPlainText_ReturnsEmpty() {
        String result = ciphers.encrypt("NO_OP_CIPHER", "");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testEncrypt_NoVector_UnsupportedCipher_ThrowsException() {
        String plainText = "test message";
        assertThrows(AgentStudioException.class, () -> ciphers.encrypt("UNSUPPORTED_CIPHER", plainText));
    }

    @Test
    public void testDecrypt_NoVector_BlankCipherText_ReturnsEmpty() {
        String result = ciphers.decrypt("   ");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testDecrypt_NoVector_NullCipherText_ReturnsEmpty() {
        String result = ciphers.decrypt((String) null);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testDecrypt_NoVector_WithDefaultCipher() {
        String plainText = "test message";
        String encrypted = ciphers.encrypt(plainText);
        String decrypted = ciphers.decrypt(encrypted);
        assertNotNull(decrypted);
    }

    @Test
    public void testEncryptDecrypt_RoundTrip_NoVector() {
        String original = "Hello, World!";
        String encrypted = ciphers.encrypt(original);
        String decrypted = ciphers.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecrypt_RoundTrip_WithVector() {
        String original = "Hello, World!";
        byte[] vector = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        String encrypted = ciphers.encrypt(original, vector);
        String decrypted = ciphers.decrypt(encrypted, vector);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecrypt_SpecialCharacters() {
        String original = "中文测试!@#$%^&*()";
        String encrypted = ciphers.encrypt(original);
        String decrypted = ciphers.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
}
