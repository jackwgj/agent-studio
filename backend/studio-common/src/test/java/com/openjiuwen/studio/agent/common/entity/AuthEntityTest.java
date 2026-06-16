/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthEntityTest {

    @Test
    void testGettersAndSetters() {
        AuthEntity entity = new AuthEntity();
        IdentityEntity identity = new IdentityEntity();
        ScopeEntity scope = new ScopeEntity();
        entity.setIdentity(identity);
        entity.setScope(scope);
        assertEquals(identity, entity.getIdentity());
        assertEquals(scope, entity.getScope());
    }

    @Test
    void testDefaultValues() {
        AuthEntity entity = new AuthEntity();
        assertNull(entity.getIdentity());
        assertNull(entity.getScope());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthEntity entity1 = new AuthEntity();
        AuthEntity entity2 = new AuthEntity();
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testToString() {
        AuthEntity entity = new AuthEntity();
        assertNotNull(entity.toString());
    }

    @Test
    void testWithNestedEntities() {
        AuthEntity entity = new AuthEntity();
        IdentityEntity identity = new IdentityEntity();
        AssumeRoleEntity assumeRole = new AssumeRoleEntity();
        assumeRole.setDomainId("d1");
        identity.setAssumeRole(assumeRole);
        ScopeEntity scope = new ScopeEntity();
        ProjectEntity project = new ProjectEntity();
        project.setId("p1");
        scope.setProject(project);
        entity.setIdentity(identity);
        entity.setScope(scope);
        assertEquals("d1", entity.getIdentity().getAssumeRole().getDomainId());
        assertEquals("p1", entity.getScope().getProject().getId());
    }
}
