/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenCredentialTest {

    @Test
    void testBuilder() {
        TokenCredential credential = TokenCredential.builder().id("token123").build();
        assertEquals("token123", credential.getId());
    }

    @Test
    void testSetters() {
        TokenCredential credential = TokenCredential.builder().build();
        credential.setId("newId");
        assertEquals("newId", credential.getId());
    }

    @Test
    void testEqualsAndHashCode() {
        TokenCredential cred1 = TokenCredential.builder().id("t1").build();
        TokenCredential cred2 = TokenCredential.builder().id("t1").build();
        assertEquals(cred1, cred2);
        assertEquals(cred1.hashCode(), cred2.hashCode());
    }

    @Test
    void testToString_ExcludesId() {
        TokenCredential credential = TokenCredential.builder().id("secretId").build();
        String str = credential.toString();
        assertNotNull(str);
        assertFalse(str.contains("secretId"));
    }
}
