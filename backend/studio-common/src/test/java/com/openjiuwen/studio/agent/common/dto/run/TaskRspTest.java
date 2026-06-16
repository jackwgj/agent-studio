/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Date;

class TaskRspTest {

    @Test
    void testSetGetId() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getId());
        TaskRsp result = rsp.setId("task-001");
        assertSame(rsp, result);
        assertEquals("task-001", rsp.getId());
    }

    @Test
    void testSetGetName() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getName());
        TaskRsp result = rsp.setName("My Task");
        assertSame(rsp, result);
        assertEquals("My Task", rsp.getName());
    }

    @Test
    void testSetGetConversationId() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getConversationId());
        TaskRsp result = rsp.setConversationId("conv-001");
        assertSame(rsp, result);
        assertEquals("conv-001", rsp.getConversationId());
    }

    @Test
    void testSetGetIsPublished() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.isIsPublished());
        TaskRsp result = rsp.setIsPublished(true);
        assertSame(rsp, result);
        assertTrue(rsp.isIsPublished());
    }

    @Test
    void testSetGetWorkflow() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getWorkflow());
        TaskRspWorkflow wf = new TaskRspWorkflow().setId("wf-001");
        TaskRsp result = rsp.setWorkflow(wf);
        assertSame(rsp, result);
        assertEquals(wf, rsp.getWorkflow());
    }

    @Test
    void testSetGetStatus() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getStatus());
        TaskRsp result = rsp.setStatus(TaskStatus.RUNNING);
        assertSame(rsp, result);
        assertEquals(TaskStatus.RUNNING, rsp.getStatus());
    }

    @Test
    void testSetGetMessage() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getMessage());
        TaskRsp result = rsp.setMessage("task message");
        assertSame(rsp, result);
        assertEquals("task message", rsp.getMessage());
    }

    @Test
    void testSetGetCreateTime() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getCreateTime());
        Date now = new Date();
        TaskRsp result = rsp.setCreateTime(now);
        assertSame(rsp, result);
        assertEquals(now, rsp.getCreateTime());
    }

    @Test
    void testSetGetStartTime() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getStartTime());
        Date now = new Date();
        TaskRsp result = rsp.setStartTime(now);
        assertSame(rsp, result);
        assertEquals(now, rsp.getStartTime());
    }

    @Test
    void testSetGetUpdateTime() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getUpdateTime());
        Date now = new Date();
        TaskRsp result = rsp.setUpdateTime(now);
        assertSame(rsp, result);
        assertEquals(now, rsp.getUpdateTime());
    }

    @Test
    void testSetGetFinishTime() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getFinishTime());
        Date now = new Date();
        TaskRsp result = rsp.setFinishTime(now);
        assertSame(rsp, result);
        assertEquals(now, rsp.getFinishTime());
    }

    @Test
    void testSetGetInputs() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getInputs());
        Object inputs = "inputData";
        TaskRsp result = rsp.setInputs(inputs);
        assertSame(rsp, result);
        assertEquals(inputs, rsp.getInputs());
    }

    @Test
    void testSetGetOutputs() {
        TaskRsp rsp = new TaskRsp();
        assertNull(rsp.getOutputs());
        Object outputs = "outputData";
        TaskRsp result = rsp.setOutputs(outputs);
        assertSame(rsp, result);
        assertEquals(outputs, rsp.getOutputs());
    }

    @Test
    void testChainedSetters() {
        Date now = new Date();
        TaskRsp rsp = new TaskRsp()
                .setId("task-001")
                .setName("My Task")
                .setStatus(TaskStatus.COMPLETED)
                .setCreateTime(now);
        assertEquals("task-001", rsp.getId());
        assertEquals("My Task", rsp.getName());
        assertEquals(TaskStatus.COMPLETED, rsp.getStatus());
        assertEquals(now, rsp.getCreateTime());
    }

    @Test
    void testEquals_SameInstance() {
        TaskRsp rsp = new TaskRsp().setId("task-001");
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObjects() {
        TaskRsp r1 = new TaskRsp().setId("task-001").setName("Task");
        TaskRsp r2 = new TaskRsp().setId("task-001").setName("Task");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        TaskRsp r1 = new TaskRsp().setId("task-001");
        TaskRsp r2 = new TaskRsp().setId("task-002");
        assertNotEquals(r1, r2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        TaskRsp rsp = new TaskRsp();
        assertNotEquals(null, rsp);
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        TaskRsp r1 = new TaskRsp().setId("task-001").setName("Task");
        TaskRsp r2 = new TaskRsp().setId("task-001").setName("Task");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        TaskRsp rsp = new TaskRsp().setId("task-001").setName("Task");
        String str = rsp.toString();
        assertNotNull(str);
        assertTrue(str.contains("TaskRsp"));
        assertTrue(str.contains("task-001"));
        assertTrue(str.contains("Task"));
    }

    @Test
    void testToString_NullFields() {
        TaskRsp rsp = new TaskRsp();
        String str = rsp.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
