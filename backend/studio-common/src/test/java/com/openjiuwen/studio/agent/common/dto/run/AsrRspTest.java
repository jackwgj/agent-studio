/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsrRspTest {

    @Test
    void testGettersAndSetters_AllFields() {
        Result result = new Result().setText("hello");
        AsrRsp rsp = new AsrRsp()
                .setTraceId("trace-123")
                .setResult(result);

        assertEquals("trace-123", rsp.getTraceId());
        assertSame(result, rsp.getResult());
    }

    @Test
    void testSetters_ReturnThis() {
        AsrRsp rsp = new AsrRsp();
        assertSame(rsp, rsp.setTraceId("id"));
        assertSame(rsp, rsp.setResult(new Result()));
    }

    @Test
    void testEquals_SameObject() {
        AsrRsp rsp = new AsrRsp().setTraceId("id");
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObject() {
        AsrRsp r1 = new AsrRsp().setTraceId("id").setResult(null);
        AsrRsp r2 = new AsrRsp().setTraceId("id").setResult(null);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        AsrRsp rsp = new AsrRsp();
        assertNotEquals(null, rsp);
    }

    @Test
    void testEquals_DifferentClass() {
        AsrRsp rsp = new AsrRsp();
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        AsrRsp r1 = new AsrRsp().setTraceId("id");
        AsrRsp r2 = new AsrRsp().setTraceId("id");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AsrRsp rsp = new AsrRsp().setTraceId("id");
        assertNotNull(rsp.toString());
    }
}
