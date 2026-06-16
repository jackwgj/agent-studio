/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenResponseTest {

    @Test
    void testNoArgsConstructor() {
        TokenResponse response = new TokenResponse();
        assertNotNull(response);
        assertNull(response.getToken());
    }

    @Test
    void testBuilder() {
        TokenResponse.Domain domain = TokenResponse.Domain.builder()
                .id("dom-1").name("domain-name").build();
        TokenResponse.User user = TokenResponse.User.builder()
                .id("user-1").name("user-name").domain(domain).passwordExpiresAt("2026-12-31").build();
        TokenResponse.Project project = TokenResponse.Project.builder()
                .id("proj-1").name("project-name").domain(domain).build();
        TokenResponse.Role role = TokenResponse.Role.builder()
                .id("role-1").name("admin").build();
        TokenResponse.Endpoint endpoint = TokenResponse.Endpoint.builder()
                .id("ep-1").endpointInterface("public").region("cn-north").regionId("r-1").url("http://example.com").build();
        TokenResponse.ServiceCatalog catalog = TokenResponse.ServiceCatalog.builder()
                .id("svc-1").name("service").type("compute").endpoints(List.of(endpoint)).build();
        TokenResponse.Token token = TokenResponse.Token.builder()
                .expiresAt("2026-12-31T00:00:00Z")
                .methods(List.of("password"))
                .catalog(List.of(catalog))
                .roles(List.of(role))
                .project(project)
                .issuedAt("2026-01-01T00:00:00Z")
                .user(user)
                .build();
        TokenResponse response = TokenResponse.builder().token(token).build();

        assertNotNull(response.getToken());
        assertEquals("2026-12-31T00:00:00Z", response.getToken().getExpiresAt());
        assertEquals(1, response.getToken().getMethods().size());
        assertEquals("password", response.getToken().getMethods().get(0));
        assertEquals(1, response.getToken().getCatalog().size());
        assertEquals("svc-1", response.getToken().getCatalog().get(0).getId());
        assertEquals(1, response.getToken().getRoles().size());
        assertEquals("admin", response.getToken().getRoles().get(0).getName());
        assertEquals("proj-1", response.getToken().getProject().getId());
        assertEquals("user-1", response.getToken().getUser().getId());
        assertEquals("dom-1", response.getToken().getUser().getDomain().getId());
    }

    @Test
    void testAllArgsConstructor() {
        TokenResponse.Token token = new TokenResponse.Token();
        TokenResponse response = new TokenResponse(token);
        assertEquals(token, response.getToken());
    }

    @Test
    void testDomainBuilder() {
        TokenResponse.Domain domain = TokenResponse.Domain.builder().id("d1").name("dn").build();
        assertEquals("d1", domain.getId());
        assertEquals("dn", domain.getName());
    }

    @Test
    void testEndpointBuilder() {
        TokenResponse.Endpoint ep = TokenResponse.Endpoint.builder()
                .id("e1").endpointInterface("internal").region("us-east").regionId("r1").url("http://test").build();
        assertEquals("e1", ep.getId());
        assertEquals("internal", ep.getEndpointInterface());
        assertEquals("us-east", ep.getRegion());
        assertEquals("r1", ep.getRegionId());
        assertEquals("http://test", ep.getUrl());
    }

    @Test
    void testSettersAndGetters() {
        TokenResponse response = new TokenResponse();
        TokenResponse.Token token = new TokenResponse.Token();
        token.setExpiresAt("exp");
        token.setIssuedAt("iss");
        token.setMethods(List.of("m1"));
        response.setToken(token);
        assertEquals("exp", response.getToken().getExpiresAt());
        assertEquals("iss", response.getToken().getIssuedAt());
    }

    @Test
    void testProjectWithDomain() {
        TokenResponse.Domain domain = new TokenResponse.Domain();
        domain.setId("d1");
        domain.setName("dn");
        TokenResponse.Project project = new TokenResponse.Project();
        project.setId("p1");
        project.setName("pn");
        project.setDomain(domain);
        assertEquals("d1", project.getDomain().getId());
    }
}
