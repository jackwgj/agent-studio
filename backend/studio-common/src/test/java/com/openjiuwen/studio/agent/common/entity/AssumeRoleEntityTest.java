/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssumeRoleEntityTest {

    @Test
    void testGettersAndSetters() {
        AssumeRoleEntity entity = new AssumeRoleEntity();
        entity.setDomainId("domain123");
        assertEquals("domain123", entity.getDomainId());
    }

    @Test
    void testDefaultValues() {
        AssumeRoleEntity entity = new AssumeRoleEntity();
        assertNull(entity.getDomainId());
    }

    @Test
    void testEqualsAndHashCode() {
        AssumeRoleEntity entity1 = new AssumeRoleEntity();
        entity1.setDomainId("d1");
        AssumeRoleEntity entity2 = new AssumeRoleEntity();
        entity2.setDomainId("d1");
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testToString() {
        AssumeRoleEntity entity = new AssumeRoleEntity();
        entity.setDomainId("d1");
        assertNotNull(entity.toString());
    }
}
