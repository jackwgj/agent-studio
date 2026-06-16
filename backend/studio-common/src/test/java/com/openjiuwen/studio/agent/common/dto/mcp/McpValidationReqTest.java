/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class McpValidationReqTest {

    @Test
    void testGettersAndSetters_AllFields() {
        McpValidationReq req = new McpValidationReq()
                .setUrl("http://example.com")
                .setAuth(null);

        assertEquals("http://example.com", req.getUrl());
        assertNull(req.getAuth());
    }

    @Test
    void testSetters_ReturnThis() {
        McpValidationReq req = new McpValidationReq();
        assertSame(req, req.setUrl("url"));
        assertSame(req, req.setAuth(null));
    }

    @Test
    void testEquals_SameObject() {
        McpValidationReq req = new McpValidationReq().setUrl("url");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObject() {
        McpValidationReq r1 = new McpValidationReq().setUrl("url").setAuth(null);
        McpValidationReq r2 = new McpValidationReq().setUrl("url").setAuth(null);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        McpValidationReq req = new McpValidationReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        McpValidationReq req = new McpValidationReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        McpValidationReq r1 = new McpValidationReq().setUrl("url");
        McpValidationReq r2 = new McpValidationReq().setUrl("url");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        McpValidationReq req = new McpValidationReq().setUrl("url");
        assertNotNull(req.toString());
    }
}
