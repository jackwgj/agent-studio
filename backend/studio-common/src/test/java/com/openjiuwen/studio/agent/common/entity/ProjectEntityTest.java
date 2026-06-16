/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProjectEntityTest {

    @Test
    void testGettersAndSetters() {
        ProjectEntity entity = new ProjectEntity();
        entity.setId("project123");
        assertEquals("project123", entity.getId());
    }

    @Test
    void testDefaultValues() {
        ProjectEntity entity = new ProjectEntity();
        assertNull(entity.getId());
    }

    @Test
    void testEqualsAndHashCode() {
        ProjectEntity entity1 = new ProjectEntity();
        entity1.setId("p1");
        ProjectEntity entity2 = new ProjectEntity();
        entity2.setId("p1");
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testToString() {
        ProjectEntity entity = new ProjectEntity();
        entity.setId("p1");
        assertNotNull(entity.toString());
    }
}
