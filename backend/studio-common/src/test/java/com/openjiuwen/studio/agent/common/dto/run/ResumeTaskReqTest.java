/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResumeTaskReqTest {

    @Test
    void testGettersAndSetters_AllFields() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("key", "value");
        ResumeTaskReq req = new ResumeTaskReq()
                .setInputs(inputs)
                .setTimeout(120);

        assertSame(inputs, req.getInputs());
        assertEquals(120, req.getTimeout());
    }

    @Test
    void testSetters_ReturnThis() {
        ResumeTaskReq req = new ResumeTaskReq();
        assertSame(req, req.setInputs(new HashMap<>()));
        assertSame(req, req.setTimeout(60));
    }

    @Test
    void testEquals_SameObject() {
        ResumeTaskReq req = new ResumeTaskReq().setTimeout(60);
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObject() {
        ResumeTaskReq r1 = new ResumeTaskReq().setInputs(null).setTimeout(60);
        ResumeTaskReq r2 = new ResumeTaskReq().setInputs(null).setTimeout(60);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        ResumeTaskReq req = new ResumeTaskReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        ResumeTaskReq req = new ResumeTaskReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        ResumeTaskReq r1 = new ResumeTaskReq().setTimeout(60);
        ResumeTaskReq r2 = new ResumeTaskReq().setTimeout(60);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ResumeTaskReq req = new ResumeTaskReq().setTimeout(60);
        assertNotNull(req.toString());
    }
}
