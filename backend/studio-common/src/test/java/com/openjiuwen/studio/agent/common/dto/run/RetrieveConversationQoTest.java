/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetrieveConversationQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        RetrieveConversationQo qo = new RetrieveConversationQo()
                .setVersionId("v2");

        assertEquals("v2", qo.getVersionId());
    }

    @Test
    void testSetters_ReturnThis() {
        RetrieveConversationQo qo = new RetrieveConversationQo();
        assertSame(qo, qo.setVersionId("v"));
    }

    @Test
    void testEquals_SameObject() {
        RetrieveConversationQo qo = new RetrieveConversationQo().setVersionId("v");
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        RetrieveConversationQo q1 = new RetrieveConversationQo().setVersionId("v");
        RetrieveConversationQo q2 = new RetrieveConversationQo().setVersionId("v");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        RetrieveConversationQo qo = new RetrieveConversationQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        RetrieveConversationQo qo = new RetrieveConversationQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        RetrieveConversationQo q1 = new RetrieveConversationQo().setVersionId("v");
        RetrieveConversationQo q2 = new RetrieveConversationQo().setVersionId("v");
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        RetrieveConversationQo qo = new RetrieveConversationQo().setVersionId("v");
        assertNotNull(qo.toString());
    }
}
