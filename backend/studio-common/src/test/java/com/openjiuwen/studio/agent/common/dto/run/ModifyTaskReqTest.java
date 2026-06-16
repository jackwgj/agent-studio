/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModifyTaskReqTest {

    @Test
    void testGettersAndSetters_AllFields() {
        ModifyTaskReq req = new ModifyTaskReq()
                .setName("task1")
                .setTimeout(300);

        assertEquals("task1", req.getName());
        assertEquals(300, req.getTimeout());
    }

    @Test
    void testSetters_ReturnThis() {
        ModifyTaskReq req = new ModifyTaskReq();
        assertSame(req, req.setName("name"));
        assertSame(req, req.setTimeout(60));
    }

    @Test
    void testEquals_SameObject() {
        ModifyTaskReq req = new ModifyTaskReq().setName("task");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObject() {
        ModifyTaskReq r1 = new ModifyTaskReq().setName("task").setTimeout(60);
        ModifyTaskReq r2 = new ModifyTaskReq().setName("task").setTimeout(60);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        ModifyTaskReq req = new ModifyTaskReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        ModifyTaskReq req = new ModifyTaskReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        ModifyTaskReq r1 = new ModifyTaskReq().setName("task").setTimeout(60);
        ModifyTaskReq r2 = new ModifyTaskReq().setName("task").setTimeout(60);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ModifyTaskReq req = new ModifyTaskReq().setName("task");
        assertNotNull(req.toString());
    }
}
