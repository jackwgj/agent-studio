/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialTypeTest {

    @Test
    void testValues() {
        assertEquals(2, CredentialType.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(CredentialType.OBS, CredentialType.valueOf("OBS"));
        assertEquals(CredentialType.DELEGATION, CredentialType.valueOf("DELEGATION"));
    }

    @Test
    void testFromValue_Existing() {
        assertEquals(CredentialType.OBS, CredentialType.fromValue("obs"));
        assertEquals(CredentialType.DELEGATION, CredentialType.fromValue("delegation"));
    }

    @Test
    void testFromValue_CaseInsensitive() {
        assertEquals(CredentialType.OBS, CredentialType.fromValue("OBS"));
        assertEquals(CredentialType.DELEGATION, CredentialType.fromValue("Delegation"));
    }

    @Test
    void testFromValue_NonExisting() {
        assertThrows(IllegalArgumentException.class, () -> CredentialType.fromValue("unknown"));
    }

    @Test
    void testToString() {
        assertEquals("obs", CredentialType.OBS.toString());
        assertEquals("delegation", CredentialType.DELEGATION.toString());
    }
}
