/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsrReqTest {

    @Test
    void testGettersAndSetters_AllFields() {
        AsrReq req = new AsrReq()
                .setConfig(null)
                .setData("base64data");

        assertNull(req.getConfig());
        assertEquals("base64data", req.getData());
    }

    @Test
    void testSetters_ReturnThis() {
        AsrReq req = new AsrReq();
        assertSame(req, req.setConfig(null));
        assertSame(req, req.setData("data"));
    }

    @Test
    void testEquals_SameObject() {
        AsrReq req = new AsrReq().setData("data");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObject() {
        AsrReq r1 = new AsrReq().setConfig(null).setData("data");
        AsrReq r2 = new AsrReq().setConfig(null).setData("data");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        AsrReq req = new AsrReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        AsrReq req = new AsrReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        AsrReq r1 = new AsrReq().setData("data");
        AsrReq r2 = new AsrReq().setData("data");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AsrReq req = new AsrReq().setData("data");
        assertNotNull(req.toString());
    }
}
