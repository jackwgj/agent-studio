/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthRequestWrapperTest {

    @Test
    void testBuilder() {
        AuthRequestWrapper.Project project = AuthRequestWrapper.Project.builder()
                .id("proj-1").name("project").build();
        AuthRequestWrapper.Domain domain = AuthRequestWrapper.Domain.builder()
                .id("dom-1").name("domain").build();
        AuthRequestWrapper.Scope scope = AuthRequestWrapper.Scope.builder()
                .project(project).domain(domain).build();
        AuthRequestWrapper.Identity identity = AuthRequestWrapper.Identity.builder()
                .methods(List.of("password")).build();
        AuthRequestWrapper.Auth auth = AuthRequestWrapper.Auth.builder()
                .identity(identity).scope(scope).build();
        AuthRequestWrapper wrapper = AuthRequestWrapper.builder()
                .auth(auth).build();

        assertNotNull(wrapper.getAuth());
        assertEquals("proj-1", wrapper.getAuth().getScope().getProject().getId());
        assertEquals("dom-1", wrapper.getAuth().getScope().getDomain().getId());
        assertEquals(1, wrapper.getAuth().getIdentity().getMethods().size());
    }

    @Test
    void testSettersAndGetters() {
        AuthRequestWrapper.Auth auth = AuthRequestWrapper.Auth.builder()
                .identity(AuthRequestWrapper.Identity.builder().methods(List.of("password")).build())
                .scope(AuthRequestWrapper.Scope.builder()
                        .project(AuthRequestWrapper.Project.builder().id("p").build())
                        .domain(AuthRequestWrapper.Domain.builder().id("d").build())
                        .build())
                .build();
        AuthRequestWrapper wrapper = AuthRequestWrapper.builder().auth(auth).build();
        assertEquals(auth, wrapper.getAuth());
    }

    @Test
    void testIdentityWithPassword() {
        PasswordCredential.User.UserBuilder userBuilder = PasswordCredential.User.builder()
                .name("user").password("pass");
        PasswordCredential.Domain.DomainBuilder domainBuilder = PasswordCredential.Domain.builder()
                .name("dom");
        PasswordCredential cred = PasswordCredential.builder()
                .user(userBuilder.domain(domainBuilder.build()).build()).build();

        AuthRequestWrapper.Identity identity = AuthRequestWrapper.Identity.builder()
                .methods(List.of("password"))
                .password(cred)
                .build();

        assertNotNull(identity.getPassword());
        assertEquals("user", identity.getPassword().getUser().getName());
        assertNull(identity.getToken());
    }

    @Test
    void testIdentityWithToken() {
        TokenCredential cred = TokenCredential.builder().id("token-id").build();
        AuthRequestWrapper.Identity identity = AuthRequestWrapper.Identity.builder()
                .methods(List.of("token"))
                .token(cred)
                .build();

        assertNotNull(identity.getToken());
        assertEquals("token-id", identity.getToken().getId());
        assertNull(identity.getPassword());
    }

    @Test
    void testProjectBuilder() {
        AuthRequestWrapper.Project project = AuthRequestWrapper.Project.builder()
                .id("p1").name("pn").build();
        assertEquals("p1", project.getId());
        assertEquals("pn", project.getName());
    }

    @Test
    void testDomainBuilder() {
        AuthRequestWrapper.Domain domain = AuthRequestWrapper.Domain.builder()
                .id("d1").name("dn").build();
        assertEquals("d1", domain.getId());
        assertEquals("dn", domain.getName());
    }

    @Test
    void testScopeBuilder() {
        AuthRequestWrapper.Scope scope = AuthRequestWrapper.Scope.builder()
                .project(AuthRequestWrapper.Project.builder().id("p1").build())
                .domain(AuthRequestWrapper.Domain.builder().id("d1").build())
                .build();
        assertEquals("p1", scope.getProject().getId());
        assertEquals("d1", scope.getDomain().getId());
    }
}
