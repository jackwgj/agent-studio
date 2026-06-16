/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SignatureUtilsTest {

    private SignatureUtils signatureUtils;

    @BeforeEach
    void setUp() {
        signatureUtils = new SignatureUtils();
        ReflectionTestUtils.setField(signatureUtils, "signatureEnable", true);
        ReflectionTestUtils.setField(signatureUtils, "secretKey", "test-secret-key");
    }

    @Test
    void testSignature_Enabled() {
        String sig = signatureUtils.signature("test-data");
        assertNotNull(sig);
    }

    @Test
    void testSignature_Disabled() {
        ReflectionTestUtils.setField(signatureUtils, "signatureEnable", false);
        assertNull(signatureUtils.signature("test-data"));
    }

    @Test
    void testVerifySignature_ValidSignature() {
        String sig = signatureUtils.signature("test-data");
        assertDoesNotThrow(() -> signatureUtils.verifySignature("test-data", sig));
    }

    @Test
    void testVerifySignature_InvalidSignature() {
        String sig = signatureUtils.signature("test-data");
        assertThrows(AgentStudioException.class,
                () -> signatureUtils.verifySignature("different-data", sig));
    }

    @Test
    void testVerifySignature_EmptySignature() {
        assertThrows(AgentStudioException.class,
                () -> signatureUtils.verifySignature("test-data", ""));
    }

    @Test
    void testVerifySignature_Disabled() {
        ReflectionTestUtils.setField(signatureUtils, "signatureEnable", false);
        assertDoesNotThrow(() -> signatureUtils.verifySignature("test-data", "any"));
    }

    @Test
    void testSignature_ConsistentOutput() {
        String sig1 = signatureUtils.signature("data");
        String sig2 = signatureUtils.signature("data");
        assertNotNull(sig1);
        assertNotNull(sig2);
    }
}
