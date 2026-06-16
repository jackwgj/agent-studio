/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ListAgentConversationsQoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ListAgentConversationsQo qo = new ListAgentConversationsQo()
                .setOffset(2)
                .setLimit(30)
                .setStartTime(5000L)
                .setEndTime(6000L)
                .setType("workflow");
        assertEquals(2, qo.getOffset());
        assertEquals(30, qo.getLimit());
        assertEquals(5000L, qo.getStartTime());
        assertEquals(6000L, qo.getEndTime());
        assertEquals("workflow", qo.getType());
    }

    @Test
    void testDefaults_WhenCreated() {
        ListAgentConversationsQo qo = new ListAgentConversationsQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
        assertEquals("agent", qo.getType());
    }

    @Test
    void testEquals_SameInstance() {
        ListAgentConversationsQo qo = new ListAgentConversationsQo().setOffset(1);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObjects() {
        ListAgentConversationsQo q1 = new ListAgentConversationsQo().setOffset(1).setType("agent");
        ListAgentConversationsQo q2 = new ListAgentConversationsQo().setOffset(1).setType("agent");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        ListAgentConversationsQo qo = new ListAgentConversationsQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        ListAgentConversationsQo qo = new ListAgentConversationsQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        ListAgentConversationsQo q1 = new ListAgentConversationsQo().setOffset(1).setType("agent");
        ListAgentConversationsQo q2 = new ListAgentConversationsQo().setOffset(1).setType("agent");
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListAgentConversationsQo qo = new ListAgentConversationsQo().setType("workflow");
        assertNotNull(qo.toString());
    }
}
