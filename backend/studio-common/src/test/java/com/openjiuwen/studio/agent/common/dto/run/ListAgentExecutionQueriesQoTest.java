/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListAgentExecutionQueriesQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo()
                .setOffset(5)
                .setLimit(20);

        assertEquals(5, qo.getOffset());
        assertEquals(20, qo.getLimit());
    }

    @Test
    void testDefaultValues() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testSetters_ReturnThis() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo();
        assertSame(qo, qo.setOffset(1));
        assertSame(qo, qo.setLimit(1));
    }

    @Test
    void testEquals_SameObject() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo().setOffset(1).setLimit(2);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        ListAgentExecutionQueriesQo qo1 = new ListAgentExecutionQueriesQo().setOffset(1).setLimit(2);
        ListAgentExecutionQueriesQo qo2 = new ListAgentExecutionQueriesQo().setOffset(1).setLimit(2);
        assertEquals(qo1, qo2);
    }

    @Test
    void testEquals_Null() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        ListAgentExecutionQueriesQo qo1 = new ListAgentExecutionQueriesQo().setOffset(1).setLimit(2);
        ListAgentExecutionQueriesQo qo2 = new ListAgentExecutionQueriesQo().setOffset(1).setLimit(2);
        assertEquals(qo1.hashCode(), qo2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListAgentExecutionQueriesQo qo = new ListAgentExecutionQueriesQo();
        assertNotNull(qo.toString());
    }
}
