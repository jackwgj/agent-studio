/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TaskRspWorkflowTest {

    @Test
    void testSetGetId() {
        TaskRspWorkflow wf = new TaskRspWorkflow();
        assertNull(wf.getId());
        TaskRspWorkflow result = wf.setId("wf-001");
        assertSame(wf, result);
        assertEquals("wf-001", wf.getId());
    }

    @Test
    void testSetGetType() {
        TaskRspWorkflow wf = new TaskRspWorkflow();
        assertNull(wf.getType());
        TaskRspWorkflow result = wf.setType("workflow");
        assertSame(wf, result);
        assertEquals("workflow", wf.getType());
    }

    @Test
    void testSetGetVersionId() {
        TaskRspWorkflow wf = new TaskRspWorkflow();
        assertNull(wf.getVersionId());
        TaskRspWorkflow result = wf.setVersionId("v-001");
        assertSame(wf, result);
        assertEquals("v-001", wf.getVersionId());
    }

    @Test
    void testChainedSetters() {
        TaskRspWorkflow wf = new TaskRspWorkflow()
                .setId("wf-001")
                .setType("workflow")
                .setVersionId("v-001");
        assertEquals("wf-001", wf.getId());
        assertEquals("workflow", wf.getType());
        assertEquals("v-001", wf.getVersionId());
    }

    @Test
    void testEquals_SameInstance() {
        TaskRspWorkflow wf = new TaskRspWorkflow().setId("wf-001");
        assertEquals(wf, wf);
    }

    @Test
    void testEquals_EqualObjects() {
        TaskRspWorkflow w1 = new TaskRspWorkflow().setId("wf-001").setType("workflow");
        TaskRspWorkflow w2 = new TaskRspWorkflow().setId("wf-001").setType("workflow");
        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        TaskRspWorkflow w1 = new TaskRspWorkflow().setId("wf-001");
        TaskRspWorkflow w2 = new TaskRspWorkflow().setId("wf-002");
        assertNotEquals(w1, w2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        TaskRspWorkflow wf = new TaskRspWorkflow();
        assertNotEquals(null, wf);
        assertNotEquals("string", wf);
    }

    @Test
    void testHashCode_Consistency() {
        TaskRspWorkflow w1 = new TaskRspWorkflow().setId("wf-001").setType("workflow");
        TaskRspWorkflow w2 = new TaskRspWorkflow().setId("wf-001").setType("workflow");
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testToString() {
        TaskRspWorkflow wf = new TaskRspWorkflow().setId("wf-001").setType("workflow");
        String str = wf.toString();
        assertNotNull(str);
        assertTrue(str.contains("TaskRspWorkflow"));
        assertTrue(str.contains("wf-001"));
    }

    @Test
    void testToString_NullFields() {
        TaskRspWorkflow wf = new TaskRspWorkflow();
        String str = wf.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
