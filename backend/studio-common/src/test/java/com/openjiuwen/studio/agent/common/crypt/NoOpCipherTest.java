/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.common.crypt.NoOpCipher;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class NoOpCipherTest {
    private final NoOpCipher noOpCipher = new NoOpCipher();

    @Test
    public void testName() {
        assertEquals("NO_OP_CIPHER", noOpCipher.name());
    }

    @Test
    public void testGenIV() {
        byte[] iv = noOpCipher.genIV();
        assertNotNull(iv);
        assertEquals(0, iv.length);
    }

    @Test
    public void testEncrypt_WithIV_ReturnsEncryptedBytes() {
        String plainText = "test message";
        byte[] iv = new byte[] {1, 2, 3, 4};
        byte[] result = noOpCipher.encrypt(plainText, iv);
        assertNotNull(result);
        assertEquals(plainText.getBytes(StandardCharsets.UTF_8).length, result.length);
    }

    @Test
    public void testEncrypt_BlankPlainText_ReturnsNull() {
        byte[] result = noOpCipher.encrypt("   ", new byte[] {1, 2, 3, 4});
        assertNull(result);
    }

    @Test
    public void testEncrypt_NoIV_ReturnsEncryptedBytes() {
        String plainText = "test message";
        byte[] result = noOpCipher.encrypt(plainText);
        assertNotNull(result);
        assertEquals(plainText.getBytes(StandardCharsets.UTF_8).length, result.length);
    }

    @Test
    public void testEncrypt_EmptyPlainText_ReturnsNull() {
        byte[] result = noOpCipher.encrypt("");
        assertNull(result);
    }

    @Test
    public void testDecrypt_WithIV_ReturnsDecryptedString() {
        String plainText = "test message";
        byte[] encrypted = noOpCipher.encrypt(plainText);
        String decrypted = noOpCipher.decrypt(encrypted, new byte[] {1, 2, 3, 4});
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testDecrypt_NullCipherText_ReturnsNull() {
        String result = noOpCipher.decrypt(null, new byte[] {1, 2, 3, 4});
        assertNull(result);
    }

    @Test
    public void testDecrypt_NoIV_ReturnsDecryptedString() {
        String plainText = "test message";
        byte[] encrypted = noOpCipher.encrypt(plainText);
        String decrypted = noOpCipher.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testDecrypt_NullCipherTextNoIV_ReturnsNull() {
        String result = noOpCipher.decrypt(null);
        assertNull(result);
    }

    @Test
    public void testDecrypt_EmptyCipherText_ThrowsException() {
        assertThrows(AgentStudioException.class, () -> noOpCipher.decrypt(new byte[] {}));
    }

    @Test
    public void testDecrypt_SingleByteCipherText_ReturnsSingleCharacter() {
        assertEquals("a", noOpCipher.decrypt(new byte[] {'a'}));
    }

    @Test
    public void testEncryptDecrypt_RoundTrip() {
        String original = "Hello, World!";
        byte[] encrypted = noOpCipher.encrypt(original);
        String decrypted = noOpCipher.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecrypt_WithSpecialCharacters() {
        String original = "中文测试!@#$%^&*()";
        byte[] encrypted = noOpCipher.encrypt(original);
        String decrypted = noOpCipher.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecrypt_WithSingleCharacter() {
        String original = "a";
        byte[] encrypted = noOpCipher.encrypt(original);
        String decrypted = noOpCipher.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecrypt_WithEmptyString() {
        byte[] encrypted = noOpCipher.encrypt("");
        assertNull(encrypted);
    }
}
