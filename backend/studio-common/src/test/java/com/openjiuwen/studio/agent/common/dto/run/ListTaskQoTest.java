/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ListTaskQoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        ListTaskQo qo = new ListTaskQo()
                .setStatus("running")
                .setType("workflow")
                .setSort("update_time")
                .setOrder("asc")
                .setWorkspaceId("ws-001");
        assertEquals("running", qo.getStatus());
        assertEquals("workflow", qo.getType());
        assertEquals("update_time", qo.getSort());
        assertEquals("asc", qo.getOrder());
        assertEquals("ws-001", qo.getWorkspaceId());
    }

    @Test
    void testDefaults_WhenCreated() {
        ListTaskQo qo = new ListTaskQo();
        assertEquals("create_time", qo.getSort());
        assertEquals("desc", qo.getOrder());
    }

    @Test
    void testEquals_SameInstance() {
        ListTaskQo qo = new ListTaskQo().setStatus("running");
        assertEquals(qo, qo);
    }

    @Test
    void testEquals_EqualObjects() {
        ListTaskQo q1 = new ListTaskQo().setStatus("running").setType("workflow");
        ListTaskQo q2 = new ListTaskQo().setStatus("running").setType("workflow");
        assertEquals(q1, q2);
    }

    @Test
    void testEquals_Null() {
        ListTaskQo qo = new ListTaskQo();
        assertNotEquals(null, qo);
    }

    @Test
    void testEquals_DifferentClass() {
        ListTaskQo qo = new ListTaskQo();
        assertNotEquals("string", qo);
    }

    @Test
    void testHashCode_Consistency() {
        ListTaskQo q1 = new ListTaskQo().setStatus("running").setType("workflow");
        ListTaskQo q2 = new ListTaskQo().setStatus("running").setType("workflow");
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListTaskQo qo = new ListTaskQo().setStatus("running");
        assertNotNull(qo.toString());
    }
}
