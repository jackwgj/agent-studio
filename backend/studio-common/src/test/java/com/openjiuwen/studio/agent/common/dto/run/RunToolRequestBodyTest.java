/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunToolRequestBodyTest {

    @Test
    void testGettersAndSetters_AllFields() {
        RunToolRequestBody body = new RunToolRequestBody()
                .setParameter("{\"key\":\"value\"}")
                .setToolObsKey("obs-key-1");

        assertEquals("{\"key\":\"value\"}", body.getParameter());
        assertEquals("obs-key-1", body.getToolObsKey());
    }

    @Test
    void testSetters_ReturnThis() {
        RunToolRequestBody body = new RunToolRequestBody();
        assertSame(body, body.setParameter("param"));
        assertSame(body, body.setToolObsKey("key"));
    }

    @Test
    void testEquals_SameObject() {
        RunToolRequestBody body = new RunToolRequestBody().setParameter("param");
        assertEquals(body, body);
    }

    @Test
    void testEquals_EqualObject() {
        RunToolRequestBody b1 = new RunToolRequestBody().setParameter("p").setToolObsKey("k");
        RunToolRequestBody b2 = new RunToolRequestBody().setParameter("p").setToolObsKey("k");
        assertEquals(b1, b2);
    }

    @Test
    void testEquals_Null() {
        RunToolRequestBody body = new RunToolRequestBody();
        assertNotEquals(null, body);
    }

    @Test
    void testEquals_DifferentClass() {
        RunToolRequestBody body = new RunToolRequestBody();
        assertNotEquals("string", body);
    }

    @Test
    void testHashCode_Consistency() {
        RunToolRequestBody b1 = new RunToolRequestBody().setParameter("p").setToolObsKey("k");
        RunToolRequestBody b2 = new RunToolRequestBody().setParameter("p").setToolObsKey("k");
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        RunToolRequestBody body = new RunToolRequestBody().setParameter("param");
        assertNotNull(body.toString());
    }
}
