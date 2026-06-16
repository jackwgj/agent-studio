/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CancelTaskRspTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        CancelTaskRsp rsp = new CancelTaskRsp()
                .setId("task-001")
                .setName("MyTask")
                .setWorkflowId("wf-001")
                .setStatus(TaskStatus.CANCELLED)
                .setMessage("cancelled");
        assertEquals("task-001", rsp.getId());
        assertEquals("MyTask", rsp.getName());
        assertEquals("wf-001", rsp.getWorkflowId());
        assertEquals(TaskStatus.CANCELLED, rsp.getStatus());
        assertEquals("cancelled", rsp.getMessage());
    }

    @Test
    void testEquals_SameInstance() {
        CancelTaskRsp rsp = new CancelTaskRsp().setId("task-001");
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObjects() {
        CancelTaskRsp r1 = new CancelTaskRsp().setId("task-001").setStatus(TaskStatus.CANCELLED);
        CancelTaskRsp r2 = new CancelTaskRsp().setId("task-001").setStatus(TaskStatus.CANCELLED);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        CancelTaskRsp rsp = new CancelTaskRsp();
        assertNotEquals(null, rsp);
    }

    @Test
    void testEquals_DifferentClass() {
        CancelTaskRsp rsp = new CancelTaskRsp();
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        CancelTaskRsp r1 = new CancelTaskRsp().setId("task-001").setStatus(TaskStatus.CANCELLED);
        CancelTaskRsp r2 = new CancelTaskRsp().setId("task-001").setStatus(TaskStatus.CANCELLED);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        CancelTaskRsp rsp = new CancelTaskRsp().setId("task-001");
        assertNotNull(rsp.toString());
    }
}
