/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetControllerExecutionDetailQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo()
                .setOffset(5)
                .setLimit(20);

        assertEquals(5, qo.getOffset());
        assertEquals(20, qo.getLimit());
    }

    @Test
    void testDefaultValues() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testSetters_ReturnThis() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo();
        assertSame(qo, qo.setOffset(1));
        assertSame(qo, qo.setLimit(1));
    }

    @Test
    void testEquals_SameObject() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo().setOffset(1).setLimit(2);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        GetControllerExecutionDetailQo qo1 = new GetControllerExecutionDetailQo().setOffset(1).setLimit(2);
        GetControllerExecutionDetailQo qo2 = new GetControllerExecutionDetailQo().setOffset(1).setLimit(2);
        assertEquals(qo1, qo2);
    }

    @Test
    void testEquals_Null() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        GetControllerExecutionDetailQo qo1 = new GetControllerExecutionDetailQo().setOffset(1).setLimit(2);
        GetControllerExecutionDetailQo qo2 = new GetControllerExecutionDetailQo().setOffset(1).setLimit(2);
        assertEquals(qo1.hashCode(), qo2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        GetControllerExecutionDetailQo qo = new GetControllerExecutionDetailQo();
        assertNotNull(qo.toString());
    }
}
