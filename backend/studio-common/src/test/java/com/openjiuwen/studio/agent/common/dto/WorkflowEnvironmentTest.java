/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowEnvironmentTest {

    @Test
    void testGettersAndSetters_AllFields() {
        Map<String, String> vars = new HashMap<>();
        vars.put("key", "value");
        WorkflowEnvironment env = new WorkflowEnvironment()
                .setName("prod")
                .setVariables(vars);

        assertEquals("prod", env.getName());
        assertSame(vars, env.getVariables());
    }

    @Test
    void testSetters_ReturnThis() {
        WorkflowEnvironment env = new WorkflowEnvironment();
        assertSame(env, env.setName("name"));
        assertSame(env, env.setVariables(new HashMap<>()));
    }

    @Test
    void testEquals_SameObject() {
        WorkflowEnvironment env = new WorkflowEnvironment().setName("prod");
        assertEquals(env, env);
    }

    @Test
    void testEquals_EqualObject() {
        WorkflowEnvironment e1 = new WorkflowEnvironment().setName("prod").setVariables(null);
        WorkflowEnvironment e2 = new WorkflowEnvironment().setName("prod").setVariables(null);
        assertEquals(e1, e2);
    }

    @Test
    void testEquals_Null() {
        WorkflowEnvironment env = new WorkflowEnvironment();
        assertNotEquals(null, env);
    }

    @Test
    void testEquals_DifferentClass() {
        WorkflowEnvironment env = new WorkflowEnvironment();
        assertNotEquals("string", env);
    }

    @Test
    void testHashCode_Consistency() {
        WorkflowEnvironment e1 = new WorkflowEnvironment().setName("prod");
        WorkflowEnvironment e2 = new WorkflowEnvironment().setName("prod");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        WorkflowEnvironment env = new WorkflowEnvironment().setName("prod");
        assertNotNull(env.toString());
    }
}
