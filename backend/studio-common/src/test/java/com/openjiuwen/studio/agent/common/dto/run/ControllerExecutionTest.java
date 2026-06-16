/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ControllerExecutionTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ControllerExecution ce = new ControllerExecution()
                .setExecutionId("exec-002")
                .setQuery("test query")
                .setStatus("completed")
                .setErrorInfo("no error")
                .setStartTime(8000L);
        assertEquals("exec-002", ce.getExecutionId());
        assertEquals("test query", ce.getQuery());
        assertEquals("completed", ce.getStatus());
        assertEquals("no error", ce.getErrorInfo());
        assertEquals(8000L, ce.getStartTime());
    }

    @Test
    void testEquals_SameInstance() {
        ControllerExecution ce = new ControllerExecution().setExecutionId("exec-002");
        assertEquals(ce, ce);
    }

    @Test
    void testEquals_EqualObjects() {
        ControllerExecution c1 = new ControllerExecution().setExecutionId("exec-002").setQuery("q");
        ControllerExecution c2 = new ControllerExecution().setExecutionId("exec-002").setQuery("q");
        assertEquals(c1, c2);
    }

    @Test
    void testEquals_Null() {
        ControllerExecution ce = new ControllerExecution();
        assertNotEquals(null, ce);
    }

    @Test
    void testEquals_DifferentClass() {
        ControllerExecution ce = new ControllerExecution();
        assertNotEquals("string", ce);
    }

    @Test
    void testHashCode_Consistency() {
        ControllerExecution c1 = new ControllerExecution().setExecutionId("exec-002").setQuery("q");
        ControllerExecution c2 = new ControllerExecution().setExecutionId("exec-002").setQuery("q");
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ControllerExecution ce = new ControllerExecution().setExecutionId("exec-002");
        assertNotNull(ce.toString());
    }
}
