/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AssumeRoleCredentialTest {

    @Test
    void testBuilder() {
        AssumeRoleCredential credential = AssumeRoleCredential.builder()
            .domain_id("domain123")
            .agency_name("agency456")
            .build();
        assertEquals("domain123", credential.getDomain_id());
        assertEquals("agency456", credential.getAgency_name());
    }

    @Test
    void testSetters() {
        AssumeRoleCredential credential = AssumeRoleCredential.builder().build();
        credential.setDomain_id("newDomain");
        credential.setAgency_name("newAgency");
        assertEquals("newDomain", credential.getDomain_id());
        assertEquals("newAgency", credential.getAgency_name());
    }

    @Test
    void testEqualsAndHashCode() {
        AssumeRoleCredential cred1 = AssumeRoleCredential.builder()
            .domain_id("d1").agency_name("a1").build();
        AssumeRoleCredential cred2 = AssumeRoleCredential.builder()
            .domain_id("d1").agency_name("a1").build();
        assertEquals(cred1, cred2);
        assertEquals(cred1.hashCode(), cred2.hashCode());
    }

    @Test
    void testToString() {
        AssumeRoleCredential credential = AssumeRoleCredential.builder()
            .domain_id("d1").agency_name("a1").build();
        assertNotNull(credential.toString());
    }
}
