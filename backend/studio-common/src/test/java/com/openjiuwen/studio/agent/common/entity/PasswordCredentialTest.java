/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PasswordCredentialTest {

    @Test
    void testBuilder() {
        PasswordCredential.Domain domain = PasswordCredential.Domain.builder()
            .name("domainName")
            .build();
        PasswordCredential.User user = PasswordCredential.User.builder()
            .name("userName")
            .password("password123")
            .domain(domain)
            .build();
        PasswordCredential credential = PasswordCredential.builder()
            .user(user)
            .build();

        assertEquals("userName", credential.getUser().getName());
        assertEquals("password123", credential.getUser().getPassword());
        assertEquals("domainName", credential.getUser().getDomain().getName());
    }

    @Test
    void testDomainBuilder() {
        PasswordCredential.Domain domain = PasswordCredential.Domain.builder()
            .name("testDomain")
            .build();
        assertEquals("testDomain", domain.getName());
    }

    @Test
    void testUserBuilder() {
        PasswordCredential.User user = PasswordCredential.User.builder()
            .name("user")
            .password("pass")
            .build();
        assertEquals("user", user.getName());
        assertEquals("pass", user.getPassword());
    }

    @Test
    void testSetters() {
        PasswordCredential credential = PasswordCredential.builder().build();
        PasswordCredential.User user = PasswordCredential.User.builder()
            .name("newUser").password("newPass").build();
        credential.setUser(user);
        assertEquals("newUser", credential.getUser().getName());
    }

    @Test
    void testEqualsAndHashCode() {
        PasswordCredential cred1 = PasswordCredential.builder()
            .user(PasswordCredential.User.builder().name("u").password("p").build())
            .build();
        PasswordCredential cred2 = PasswordCredential.builder()
            .user(PasswordCredential.User.builder().name("u").password("p").build())
            .build();
        assertEquals(cred1, cred2);
        assertEquals(cred1.hashCode(), cred2.hashCode());
    }

    @Test
    void testToString() {
        PasswordCredential credential = PasswordCredential.builder()
            .user(PasswordCredential.User.builder().name("u").password("p").build())
            .build();
        assertNotNull(credential.toString());
    }

    @Test
    void testDomainEquals() {
        PasswordCredential.Domain d1 = PasswordCredential.Domain.builder().name("d").build();
        PasswordCredential.Domain d2 = PasswordCredential.Domain.builder().name("d").build();
        assertEquals(d1, d2);
    }

    @Test
    void testUserEquals() {
        PasswordCredential.User u1 = PasswordCredential.User.builder().name("u").password("p").build();
        PasswordCredential.User u2 = PasswordCredential.User.builder().name("u").password("p").build();
        assertEquals(u1, u2);
    }
}
