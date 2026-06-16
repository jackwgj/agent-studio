/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class CreateTaskReqTest {

    @Test
    void testSetGetType() {
        CreateTaskReq req = new CreateTaskReq();
        assertNull(req.getType());
        CreateTaskReq result = req.setType("workflow");
        assertSame(req, result);
        assertEquals("workflow", req.getType());
    }

    @Test
    void testSetGetName() {
        CreateTaskReq req = new CreateTaskReq();
        assertNull(req.getName());
        CreateTaskReq result = req.setName("My Task");
        assertSame(req, result);
        assertEquals("My Task", req.getName());
    }

    @Test
    void testSetGetMode() {
        CreateTaskReq req = new CreateTaskReq();
        assertNull(req.getMode());
        CreateTaskReq result = req.setMode("sync");
        assertSame(req, result);
        assertEquals("sync", req.getMode());
    }

    @Test
    void testSetGetInputs() {
        CreateTaskReq req = new CreateTaskReq();
        assertNull(req.getInputs());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("key", "value");
        CreateTaskReq result = req.setInputs(inputs);
        assertSame(req, result);
        assertEquals(inputs, req.getInputs());
    }

    @Test
    void testSetGetTimeout() {
        CreateTaskReq req = new CreateTaskReq();
        assertNull(req.getTimeout());
        CreateTaskReq result = req.setTimeout(30);
        assertSame(req, result);
        assertEquals(30, req.getTimeout());
    }

    @Test
    void testSetGetVersion() {
        CreateTaskReq req = new CreateTaskReq();
        assertNull(req.getVersion());
        CreateTaskReq result = req.setVersion(1L);
        assertSame(req, result);
        assertEquals(1L, req.getVersion());
    }

    @Test
    void testChainedSetters() {
        CreateTaskReq req = new CreateTaskReq()
                .setType("workflow")
                .setName("Task")
                .setMode("sync")
                .setTimeout(60)
                .setVersion(2L);
        assertEquals("workflow", req.getType());
        assertEquals("Task", req.getName());
        assertEquals("sync", req.getMode());
        assertEquals(60, req.getTimeout());
        assertEquals(2L, req.getVersion());
    }

    @Test
    void testEquals_SameInstance() {
        CreateTaskReq req = new CreateTaskReq().setType("workflow");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObjects() {
        CreateTaskReq r1 = new CreateTaskReq().setType("workflow").setName("Task");
        CreateTaskReq r2 = new CreateTaskReq().setType("workflow").setName("Task");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        CreateTaskReq r1 = new CreateTaskReq().setType("workflow");
        CreateTaskReq r2 = new CreateTaskReq().setType("agent");
        assertNotEquals(r1, r2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        CreateTaskReq req = new CreateTaskReq();
        assertNotEquals(null, req);
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        CreateTaskReq r1 = new CreateTaskReq().setType("workflow").setName("Task");
        CreateTaskReq r2 = new CreateTaskReq().setType("workflow").setName("Task");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        CreateTaskReq req = new CreateTaskReq().setType("workflow").setName("Task");
        String str = req.toString();
        assertNotNull(str);
        assertTrue(str.contains("CreateTaskReq"));
        assertTrue(str.contains("workflow"));
    }

    @Test
    void testToString_NullFields() {
        CreateTaskReq req = new CreateTaskReq();
        String str = req.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
