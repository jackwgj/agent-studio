/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListControllerExecutionsQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo()
                .setOffset(5)
                .setLimit(20);

        assertEquals(5, qo.getOffset());
        assertEquals(20, qo.getLimit());
    }

    @Test
    void testDefaultValues() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testSetters_ReturnThis() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo();
        assertSame(qo, qo.setOffset(1));
        assertSame(qo, qo.setLimit(1));
    }

    @Test
    void testEquals_SameObject() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo().setOffset(1).setLimit(2);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        ListControllerExecutionsQo qo1 = new ListControllerExecutionsQo().setOffset(1).setLimit(2);
        ListControllerExecutionsQo qo2 = new ListControllerExecutionsQo().setOffset(1).setLimit(2);
        assertEquals(qo1, qo2);
    }

    @Test
    void testEquals_Null() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        ListControllerExecutionsQo qo1 = new ListControllerExecutionsQo().setOffset(1).setLimit(2);
        ListControllerExecutionsQo qo2 = new ListControllerExecutionsQo().setOffset(1).setLimit(2);
        assertEquals(qo1.hashCode(), qo2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListControllerExecutionsQo qo = new ListControllerExecutionsQo();
        assertNotNull(qo.toString());
    }
}
