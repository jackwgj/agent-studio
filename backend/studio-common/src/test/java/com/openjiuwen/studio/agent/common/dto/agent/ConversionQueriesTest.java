/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ConversionQueriesTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ConversionQueries queries = new ConversionQueries()
                .setCount(3)
                .setConversationInfos(new ArrayList<>());

        assertEquals(3, queries.getCount());
        assertNotNull(queries.getConversationInfos());
    }

    @Test
    void testSetters_ReturnThis() {
        ConversionQueries queries = new ConversionQueries();
        assertSame(queries, queries.setCount(1));
        assertSame(queries, queries.setConversationInfos(new ArrayList<>()));
    }

    @Test
    void testEquals_SameObject() {
        ConversionQueries queries = new ConversionQueries().setCount(1);
        assertEquals(queries, queries);
    }

    @Test
    void testEquals_EqualObject() {
        ConversionQueries q1 = new ConversionQueries().setCount(1).setConversationInfos(null);
        ConversionQueries q2 = new ConversionQueries().setCount(1).setConversationInfos(null);
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        ConversionQueries queries = new ConversionQueries();
        assertNotEquals(null, queries);
    }

    @Test
    void testEquals_DifferentClass() {
        ConversionQueries queries = new ConversionQueries();
        assertNotEquals("string", queries);
    }

    @Test
    void testHashCode_Consistency() {
        ConversionQueries q1 = new ConversionQueries().setCount(1);
        ConversionQueries q2 = new ConversionQueries().setCount(1);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ConversionQueries queries = new ConversionQueries().setCount(1);
        assertNotNull(queries.toString());
    }
}
