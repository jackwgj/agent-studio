/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ListExecutionQueriesQoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ListExecutionQueriesQo qo = new ListExecutionQueriesQo()
                .setOffset(3)
                .setLimit(50)
                .setStartTime(3000L)
                .setEndTime(4000L)
                .setVersion("v2");
        assertEquals(3, qo.getOffset());
        assertEquals(50, qo.getLimit());
        assertEquals(3000L, qo.getStartTime());
        assertEquals(4000L, qo.getEndTime());
        assertEquals("v2", qo.getVersion());
    }

    @Test
    void testDefaults_WhenCreated() {
        ListExecutionQueriesQo qo = new ListExecutionQueriesQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testEquals_SameInstance() {
        ListExecutionQueriesQo qo = new ListExecutionQueriesQo().setOffset(1);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObjects() {
        ListExecutionQueriesQo q1 = new ListExecutionQueriesQo().setOffset(1).setLimit(5).setVersion("v1");
        ListExecutionQueriesQo q2 = new ListExecutionQueriesQo().setOffset(1).setLimit(5).setVersion("v1");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        ListExecutionQueriesQo qo = new ListExecutionQueriesQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        ListExecutionQueriesQo qo = new ListExecutionQueriesQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        ListExecutionQueriesQo q1 = new ListExecutionQueriesQo().setOffset(1).setLimit(5);
        ListExecutionQueriesQo q2 = new ListExecutionQueriesQo().setOffset(1).setLimit(5);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListExecutionQueriesQo qo = new ListExecutionQueriesQo().setVersion("v2");
        assertNotNull(qo.toString());
    }
}
