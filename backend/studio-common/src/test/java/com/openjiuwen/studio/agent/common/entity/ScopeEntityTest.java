/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScopeEntityTest {

    @Test
    void testGettersAndSetters() {
        ScopeEntity entity = new ScopeEntity();
        ProjectEntity project = new ProjectEntity();
        project.setId("p1");
        entity.setProject(project);
        assertEquals("p1", entity.getProject().getId());
    }

    @Test
    void testDefaultValues() {
        ScopeEntity entity = new ScopeEntity();
        assertNull(entity.getProject());
    }

    @Test
    void testEqualsAndHashCode() {
        ScopeEntity entity1 = new ScopeEntity();
        ScopeEntity entity2 = new ScopeEntity();
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testToString() {
        ScopeEntity entity = new ScopeEntity();
        assertNotNull(entity.toString());
    }
}
