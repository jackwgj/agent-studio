/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResetUserVariableMemoryResponseBodyTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ResetUserVariableMemoryResponseBody body = new ResetUserVariableMemoryResponseBody()
                .setResult(true);

        assertTrue(body.isResult());
    }

    @Test
    void testSetters_ReturnThis() {
        ResetUserVariableMemoryResponseBody body = new ResetUserVariableMemoryResponseBody();
        assertSame(body, body.setResult(false));
    }

    @Test
    void testEquals_SameObject() {
        ResetUserVariableMemoryResponseBody body = new ResetUserVariableMemoryResponseBody().setResult(true);
        assertEquals(body, body);
    }

    @Test
    void testEquals_EqualObject() {
        ResetUserVariableMemoryResponseBody b1 = new ResetUserVariableMemoryResponseBody().setResult(true);
        ResetUserVariableMemoryResponseBody b2 = new ResetUserVariableMemoryResponseBody().setResult(true);
        assertEquals(b1, b2);
    }

    @Test
    void testEquals_Null() {
        ResetUserVariableMemoryResponseBody body = new ResetUserVariableMemoryResponseBody();
        assertNotEquals(null, body);
    }

    @Test
    void testEquals_DifferentClass() {
        ResetUserVariableMemoryResponseBody body = new ResetUserVariableMemoryResponseBody();
        assertNotEquals("string", body);
    }

    @Test
    void testHashCode_Consistency() {
        ResetUserVariableMemoryResponseBody b1 = new ResetUserVariableMemoryResponseBody().setResult(true);
        ResetUserVariableMemoryResponseBody b2 = new ResetUserVariableMemoryResponseBody().setResult(true);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ResetUserVariableMemoryResponseBody body = new ResetUserVariableMemoryResponseBody().setResult(true);
        assertNotNull(body.toString());
    }
}
