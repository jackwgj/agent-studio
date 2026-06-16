/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetrieveConversationMemoryQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        RetrieveConversationMemoryQo qo = new RetrieveConversationMemoryQo()
                .setVersionId("v1");

        assertEquals("v1", qo.getVersionId());
    }

    @Test
    void testSetters_ReturnThis() {
        RetrieveConversationMemoryQo qo = new RetrieveConversationMemoryQo();
        assertSame(qo, qo.setVersionId("v"));
    }

    @Test
    void testEquals_SameObject() {
        RetrieveConversationMemoryQo qo = new RetrieveConversationMemoryQo().setVersionId("v");
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        RetrieveConversationMemoryQo q1 = new RetrieveConversationMemoryQo().setVersionId("v");
        RetrieveConversationMemoryQo q2 = new RetrieveConversationMemoryQo().setVersionId("v");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        RetrieveConversationMemoryQo qo = new RetrieveConversationMemoryQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        RetrieveConversationMemoryQo qo = new RetrieveConversationMemoryQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        RetrieveConversationMemoryQo q1 = new RetrieveConversationMemoryQo().setVersionId("v");
        RetrieveConversationMemoryQo q2 = new RetrieveConversationMemoryQo().setVersionId("v");
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        RetrieveConversationMemoryQo qo = new RetrieveConversationMemoryQo().setVersionId("v");
        assertNotNull(qo.toString());
    }
}
