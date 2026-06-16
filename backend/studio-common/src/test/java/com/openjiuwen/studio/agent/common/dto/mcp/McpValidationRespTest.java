/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class McpValidationRespTest {

    @Test
    void testGettersAndSetters_AllFields() {
        McpValidationResp resp = new McpValidationResp()
                .setSuccess(true)
                .setErrorMsg("no error");

        assertTrue(resp.isSuccess());
        assertEquals("no error", resp.getErrorMsg());
    }

    @Test
    void testSetters_ReturnThis() {
        McpValidationResp resp = new McpValidationResp();
        assertSame(resp, resp.setSuccess(false));
        assertSame(resp, resp.setErrorMsg("msg"));
    }

    @Test
    void testEquals_SameObject() {
        McpValidationResp resp = new McpValidationResp().setSuccess(true);
        assertEquals(resp, resp);
    }

    @Test
    void testEquals_EqualObject() {
        McpValidationResp r1 = new McpValidationResp().setSuccess(true).setErrorMsg("err");
        McpValidationResp r2 = new McpValidationResp().setSuccess(true).setErrorMsg("err");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        McpValidationResp resp = new McpValidationResp();
        assertNotEquals(null, resp);
    }

    @Test
    void testEquals_DifferentClass() {
        McpValidationResp resp = new McpValidationResp();
        assertNotEquals("string", resp);
    }

    @Test
    void testHashCode_Consistency() {
        McpValidationResp r1 = new McpValidationResp().setSuccess(true).setErrorMsg("err");
        McpValidationResp r2 = new McpValidationResp().setSuccess(true).setErrorMsg("err");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        McpValidationResp resp = new McpValidationResp().setSuccess(false);
        assertNotNull(resp.toString());
    }
}
