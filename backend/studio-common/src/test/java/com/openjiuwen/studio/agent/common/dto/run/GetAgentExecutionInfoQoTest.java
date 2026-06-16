/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetAgentExecutionInfoQoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo()
                .setOffset(5)
                .setLimit(20);

        assertEquals(5, qo.getOffset());
        assertEquals(20, qo.getLimit());
    }

    @Test
    void testDefaultValues() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testSetters_ReturnThis() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo();
        assertSame(qo, qo.setOffset(1));
        assertSame(qo, qo.setLimit(1));
    }

    @Test
    void testEquals_SameObject() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo().setOffset(1).setLimit(2);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObject() {
        GetAgentExecutionInfoQo qo1 = new GetAgentExecutionInfoQo().setOffset(1).setLimit(2);
        GetAgentExecutionInfoQo qo2 = new GetAgentExecutionInfoQo().setOffset(1).setLimit(2);
        assertEquals(qo1, qo2);
    }

    @Test
    void testEquals_Null() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        GetAgentExecutionInfoQo qo1 = new GetAgentExecutionInfoQo().setOffset(1).setLimit(2);
        GetAgentExecutionInfoQo qo2 = new GetAgentExecutionInfoQo().setOffset(1).setLimit(2);
        assertEquals(qo1.hashCode(), qo2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        GetAgentExecutionInfoQo qo = new GetAgentExecutionInfoQo();
        assertNotNull(qo.toString());
    }
}
