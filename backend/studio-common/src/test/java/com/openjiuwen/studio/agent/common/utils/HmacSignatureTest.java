/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacSignatureTest {

    @Test
    void testGenerateHmac_SHA256() {
        String result = HmacSignature.generateHmac("test-data", "secret-key", "HmacSHA256");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void testGenerateHmac_SHA1() {
        String result = HmacSignature.generateHmac("test-data", "secret-key", "HmacSHA1");
        assertNotNull(result);
    }

    @Test
    void testGenerateHmac_ConsistentOutput() {
        String sig1 = HmacSignature.generateHmac("data", "key", "HmacSHA256");
        String sig2 = HmacSignature.generateHmac("data", "key", "HmacSHA256");
        assertTrue(sig1.equals(sig2));
    }

    @Test
    void testGenerateHmac_DifferentDataProducesDifferentSignature() {
        String sig1 = HmacSignature.generateHmac("data1", "key", "HmacSHA256");
        String sig2 = HmacSignature.generateHmac("data2", "key", "HmacSHA256");
        assertTrue(!sig1.equals(sig2));
    }

    @Test
    void testGenerateHmac_DifferentKeyProducesDifferentSignature() {
        String sig1 = HmacSignature.generateHmac("data", "key1", "HmacSHA256");
        String sig2 = HmacSignature.generateHmac("data", "key2", "HmacSHA256");
        assertTrue(!sig1.equals(sig2));
    }

    @Test
    void testGenerateHmac_InvalidAlgorithm() {
        assertThrows(AgentStudioException.class,
                () -> HmacSignature.generateHmac("data", "key", "InvalidAlgo"));
    }

    @Test
    void testGenerateHmac_EmptyData() {
        String result = HmacSignature.generateHmac("", "key", "HmacSHA256");
        assertNotNull(result);
    }

    @Test
    void testGenerateHmac_EmptyKey() {
        assertThrows(IllegalArgumentException.class, () -> HmacSignature.generateHmac("data", "", "HmacSHA256"));
    }
}
