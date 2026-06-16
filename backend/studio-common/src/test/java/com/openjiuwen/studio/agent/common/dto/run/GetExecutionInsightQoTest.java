/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class GetExecutionInsightQoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        GetExecutionInsightQo qo = new GetExecutionInsightQo()
                .setOffset(7)
                .setLimit(25)
                .setVersion("v3");
        assertEquals(7, qo.getOffset());
        assertEquals(25, qo.getLimit());
        assertEquals("v3", qo.getVersion());
    }

    @Test
    void testDefaults_WhenCreated() {
        GetExecutionInsightQo qo = new GetExecutionInsightQo();
        assertEquals(0, qo.getOffset());
        assertEquals(10, qo.getLimit());
    }

    @Test
    void testEquals_SameInstance() {
        GetExecutionInsightQo qo = new GetExecutionInsightQo().setOffset(1);
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObjects() {
        GetExecutionInsightQo q1 = new GetExecutionInsightQo().setOffset(1).setVersion("v1");
        GetExecutionInsightQo q2 = new GetExecutionInsightQo().setOffset(1).setVersion("v1");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        GetExecutionInsightQo qo = new GetExecutionInsightQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        GetExecutionInsightQo qo = new GetExecutionInsightQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        GetExecutionInsightQo q1 = new GetExecutionInsightQo().setOffset(1).setVersion("v1");
        GetExecutionInsightQo q2 = new GetExecutionInsightQo().setOffset(1).setVersion("v1");
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        GetExecutionInsightQo qo = new GetExecutionInsightQo().setVersion("v3");
        assertNotNull(qo.toString());
    }
}
