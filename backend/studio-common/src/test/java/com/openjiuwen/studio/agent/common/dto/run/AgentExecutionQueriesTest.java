/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AgentExecutionQueriesTest {

    @Test
    void testGettersAndSetters_AllFields() {
        AgentExecutionQueries queries = new AgentExecutionQueries()
                .setCount(10)
                .setExecutionQueries(new ArrayList<>());

        assertEquals(10, queries.getCount());
        assertNotNull(queries.getExecutionQueries());
    }

    @Test
    void testSetters_ReturnThis() {
        AgentExecutionQueries queries = new AgentExecutionQueries();
        assertSame(queries, queries.setCount(1));
        assertSame(queries, queries.setExecutionQueries(new ArrayList<>()));
    }

    @Test
    void testEquals_SameObject() {
        AgentExecutionQueries queries = new AgentExecutionQueries().setCount(1);
        assertEquals(queries, queries);
    }

    @Test
    void testEquals_EqualObject() {
        AgentExecutionQueries q1 = new AgentExecutionQueries().setCount(1).setExecutionQueries(null);
        AgentExecutionQueries q2 = new AgentExecutionQueries().setCount(1).setExecutionQueries(null);
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        AgentExecutionQueries queries = new AgentExecutionQueries();
        assertNotEquals(null, queries);
    }

    @Test
    void testEquals_DifferentClass() {
        AgentExecutionQueries queries = new AgentExecutionQueries();
        assertNotEquals("string", queries);
    }

    @Test
    void testHashCode_Consistency() {
        AgentExecutionQueries q1 = new AgentExecutionQueries().setCount(1);
        AgentExecutionQueries q2 = new AgentExecutionQueries().setCount(1);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AgentExecutionQueries queries = new AgentExecutionQueries().setCount(1);
        assertNotNull(queries.toString());
    }
}
