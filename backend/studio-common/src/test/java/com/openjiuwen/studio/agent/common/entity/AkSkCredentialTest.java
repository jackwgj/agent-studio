/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AkSkCredentialTest {

    @Test
    void testConstructor() {
        AkSkCredential credential = new AkSkCredential("accessKey", "secretKey");
        assertNotNull(credential.getAccess());
        assertNotNull(credential.getSecret());
        assertEquals("accessKey", credential.getAccess().getKey());
        assertEquals("secretKey", credential.getSecret().getKey());
    }

    @Test
    void testAccessKeyBuilder() {
        AkSkCredential.AccessKey accessKey = AkSkCredential.AccessKey.builder().key("testKey").build();
        assertEquals("testKey", accessKey.getKey());
    }

    @Test
    void testSetters() {
        AkSkCredential credential = new AkSkCredential("ak", "sk");
        AkSkCredential.AccessKey newAccess = AkSkCredential.AccessKey.builder().key("newAk").build();
        AkSkCredential.AccessKey newSecret = AkSkCredential.AccessKey.builder().key("newSk").build();
        credential.setAccess(newAccess);
        credential.setSecret(newSecret);
        assertEquals("newAk", credential.getAccess().getKey());
        assertEquals("newSk", credential.getSecret().getKey());
    }

    @Test
    void testEqualsAndHashCode() {
        AkSkCredential cred1 = new AkSkCredential("ak", "sk");
        AkSkCredential cred2 = new AkSkCredential("ak", "sk");
        assertEquals(cred1, cred2);
        assertEquals(cred1.hashCode(), cred2.hashCode());
    }

    @Test
    void testToString() {
        AkSkCredential credential = new AkSkCredential("ak", "sk");
        assertNotNull(credential.toString());
    }

    @Test
    void testAccessKeyEquals() {
        AkSkCredential.AccessKey key1 = AkSkCredential.AccessKey.builder().key("test").build();
        AkSkCredential.AccessKey key2 = AkSkCredential.AccessKey.builder().key("test").build();
        assertEquals(key1, key2);
    }
}
