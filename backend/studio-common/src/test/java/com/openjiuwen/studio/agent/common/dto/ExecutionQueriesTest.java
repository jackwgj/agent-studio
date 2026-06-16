/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionQueriesTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ExecutionQueries queries = new ExecutionQueries()
                .setCount(5)
                .setExecutionInfos(new ArrayList<>());

        assertEquals(5, queries.getCount());
        assertNotNull(queries.getExecutionInfos());
    }

    @Test
    void testSetters_ReturnThis() {
        ExecutionQueries queries = new ExecutionQueries();
        assertSame(queries, queries.setCount(1));
        assertSame(queries, queries.setExecutionInfos(new ArrayList<>()));
    }

    @Test
    void testEquals_SameObject() {
        ExecutionQueries queries = new ExecutionQueries().setCount(1);
        assertEquals(queries, queries);
    }

    @Test
    void testEquals_EqualObject() {
        ExecutionQueries q1 = new ExecutionQueries().setCount(1).setExecutionInfos(null);
        ExecutionQueries q2 = new ExecutionQueries().setCount(1).setExecutionInfos(null);
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        ExecutionQueries queries = new ExecutionQueries();
        assertNotEquals(null, queries);
    }

    @Test
    void testEquals_DifferentClass() {
        ExecutionQueries queries = new ExecutionQueries();
        assertNotEquals("string", queries);
    }

    @Test
    void testHashCode_Consistency() {
        ExecutionQueries q1 = new ExecutionQueries().setCount(1);
        ExecutionQueries q2 = new ExecutionQueries().setCount(1);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ExecutionQueries queries = new ExecutionQueries().setCount(1);
        assertNotNull(queries.toString());
    }
}
