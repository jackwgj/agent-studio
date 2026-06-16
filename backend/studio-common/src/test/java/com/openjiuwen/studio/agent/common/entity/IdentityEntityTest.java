/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdentityEntityTest {

    @Test
    void testGettersAndSetters() {
        IdentityEntity entity = new IdentityEntity();
        List<String> methods = Arrays.asList("password", "token");
        entity.setMethods(methods);
        assertEquals(methods, entity.getMethods());
    }

    @Test
    void testAssumeRole() {
        IdentityEntity entity = new IdentityEntity();
        AssumeRoleEntity assumeRole = new AssumeRoleEntity();
        assumeRole.setDomainId("d1");
        entity.setAssumeRole(assumeRole);
        assertEquals("d1", entity.getAssumeRole().getDomainId());
    }

    @Test
    void testDefaultValues() {
        IdentityEntity entity = new IdentityEntity();
        assertNull(entity.getMethods());
        assertNull(entity.getAssumeRole());
    }

    @Test
    void testEqualsAndHashCode() {
        IdentityEntity entity1 = new IdentityEntity();
        entity1.setMethods(Arrays.asList("password"));
        IdentityEntity entity2 = new IdentityEntity();
        entity2.setMethods(Arrays.asList("password"));
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testToString() {
        IdentityEntity entity = new IdentityEntity();
        assertNotNull(entity.toString());
    }
}
