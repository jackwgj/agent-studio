/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetrieveTaskQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        RetrieveTaskQo qo = new RetrieveTaskQo()
                .setWorkspaceId("ws-123");

        assertEquals("ws-123", qo.getWorkspaceId());
    }

    @Test
    void testSetters_ReturnThis() {
        RetrieveTaskQo qo = new RetrieveTaskQo();
        assertSame(qo, qo.setWorkspaceId("id"));
    }

    @Test
    void testEquals_SameObject() {
        RetrieveTaskQo qo = new RetrieveTaskQo().setWorkspaceId("id");
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        RetrieveTaskQo q1 = new RetrieveTaskQo().setWorkspaceId("id");
        RetrieveTaskQo q2 = new RetrieveTaskQo().setWorkspaceId("id");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        RetrieveTaskQo qo = new RetrieveTaskQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        RetrieveTaskQo qo = new RetrieveTaskQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        RetrieveTaskQo q1 = new RetrieveTaskQo().setWorkspaceId("id");
        RetrieveTaskQo q2 = new RetrieveTaskQo().setWorkspaceId("id");
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        RetrieveTaskQo qo = new RetrieveTaskQo().setWorkspaceId("id");
        assertNotNull(qo.toString());
    }
}
