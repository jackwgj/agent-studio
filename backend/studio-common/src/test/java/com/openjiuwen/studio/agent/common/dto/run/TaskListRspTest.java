/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class TaskListRspTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        TaskListItem item = new TaskListItem().setId("item-001").setName("Item1");
        TaskListRsp rsp = new TaskListRsp()
                .setWorkflowId("wf-001")
                .setCount(1L)
                .setData(List.of(item));
        assertEquals("wf-001", rsp.getWorkflowId());
        assertEquals(1L, rsp.getCount());
        assertEquals(1, rsp.getData().size());
        assertEquals(item, rsp.getData().get(0));
    }

    @Test
    void testEquals_SameInstance() {
        TaskListRsp rsp = new TaskListRsp().setWorkflowId("wf");
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObjects() {
        TaskListRsp r1 = new TaskListRsp().setWorkflowId("wf").setCount(5L);
        TaskListRsp r2 = new TaskListRsp().setWorkflowId("wf").setCount(5L);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        TaskListRsp rsp = new TaskListRsp();
        assertNotEquals(null, rsp);
    }

    @Test
    void testEquals_DifferentClass() {
        TaskListRsp rsp = new TaskListRsp();
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        TaskListRsp r1 = new TaskListRsp().setWorkflowId("wf").setCount(5L);
        TaskListRsp r2 = new TaskListRsp().setWorkflowId("wf").setCount(5L);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        TaskListRsp rsp = new TaskListRsp().setWorkflowId("wf");
        assertNotNull(rsp.toString());
    }
}
