/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ListConversationQueriesQoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ListConversationQueriesQo qo = new ListConversationQueriesQo()
                .setOffset(5)
                .setLimit(20)
                .setStartTime(1000L)
                .setEndTime(2000L)
                .setVersion("v1");
        assertEquals(5, qo.getOffset());
        assertEquals(20, qo.getLimit());
        assertEquals(1000L, qo.getStartTime());
        assertEquals(2000L, qo.getEndTime());
        assertEquals("v1", qo.getVersion());
    }

    @Test
    void testDefaults_WhenCreated() {
        ListConversationQueriesQo qo = new ListConversationQueriesQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testEquals_SameInstance() {
        ListConversationQueriesQo qo = new ListConversationQueriesQo().setOffset(1);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObjects() {
        ListConversationQueriesQo q1 = new ListConversationQueriesQo().setOffset(1).setLimit(5).setVersion("v1");
        ListConversationQueriesQo q2 = new ListConversationQueriesQo().setOffset(1).setLimit(5).setVersion("v1");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        ListConversationQueriesQo qo = new ListConversationQueriesQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        ListConversationQueriesQo qo = new ListConversationQueriesQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        ListConversationQueriesQo q1 = new ListConversationQueriesQo().setOffset(1).setLimit(5);
        ListConversationQueriesQo q2 = new ListConversationQueriesQo().setOffset(1).setLimit(5);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListConversationQueriesQo qo = new ListConversationQueriesQo().setVersion("v1");
        assertNotNull(qo.toString());
    }
}
