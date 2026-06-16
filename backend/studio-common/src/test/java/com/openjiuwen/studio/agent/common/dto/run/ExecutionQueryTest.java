/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ExecutionQueryTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ExecutionQuery eq = new ExecutionQuery()
                .setExecutionId("exec-001")
                .setQuery("hello")
                .setStatus("running")
                .setErrorInfo("none")
                .setStartTime(9000L);
        assertEquals("exec-001", eq.getExecutionId());
        assertEquals("hello", eq.getQuery());
        assertEquals("running", eq.getStatus());
        assertEquals("none", eq.getErrorInfo());
        assertEquals(9000L, eq.getStartTime());
    }

    @Test
    void testEquals_SameInstance() {
        ExecutionQuery eq = new ExecutionQuery().setExecutionId("exec-001");
        assertEquals(eq, eq);
    }

    @Test
    void testEquals_EqualObjects() {
        ExecutionQuery e1 = new ExecutionQuery().setExecutionId("exec-001").setQuery("q");
        ExecutionQuery e2 = new ExecutionQuery().setExecutionId("exec-001").setQuery("q");
        assertEquals(e1, e2);
    }

    @Test
    void testEquals_Null() {
        ExecutionQuery eq = new ExecutionQuery();
        assertNotEquals(null, eq);
    }

    @Test
    void testEquals_DifferentClass() {
        ExecutionQuery eq = new ExecutionQuery();
        assertNotEquals("string", eq);
    }

    @Test
    void testHashCode_Consistency() {
        ExecutionQuery e1 = new ExecutionQuery().setExecutionId("exec-001").setQuery("q");
        ExecutionQuery e2 = new ExecutionQuery().setExecutionId("exec-001").setQuery("q");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ExecutionQuery eq = new ExecutionQuery().setExecutionId("exec-001");
        assertNotNull(eq.toString());
    }
}
