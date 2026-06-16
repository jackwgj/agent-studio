/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.mcp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class McpCallToolRespTest {

    @Test
    void testGettersAndSetters_AllFields() {
        List<McpCallToolRespContent> contentList = new ArrayList<>();
        McpCallToolResp resp = new McpCallToolResp()
                .setIsError(true)
                .setContent(contentList);

        assertTrue(resp.isIsError());
        assertSame(contentList, resp.getContent());
    }

    @Test
    void testSetters_ReturnThis() {
        McpCallToolResp resp = new McpCallToolResp();
        assertSame(resp, resp.setIsError(false));
        assertSame(resp, resp.setContent(new ArrayList<>()));
    }

    @Test
    void testEquals_SameObject() {
        McpCallToolResp resp = new McpCallToolResp().setIsError(true);
        assertEquals(resp, resp);
    }

    @Test
    void testEquals_EqualObject() {
        McpCallToolResp resp1 = new McpCallToolResp().setIsError(true).setContent(null);
        McpCallToolResp resp2 = new McpCallToolResp().setIsError(true).setContent(null);
        assertEquals(resp1, resp2);
    }

    @Test
    void testEquals_Null() {
        McpCallToolResp resp = new McpCallToolResp();
        assertNotEquals(null, resp);
    }

    @Test
    void testEquals_DifferentClass() {
        McpCallToolResp resp = new McpCallToolResp();
        assertNotEquals("string", resp);
    }

    @Test
    void testHashCode_Consistency() {
        McpCallToolResp resp1 = new McpCallToolResp().setIsError(true);
        McpCallToolResp resp2 = new McpCallToolResp().setIsError(true);
        assertEquals(resp1.hashCode(), resp2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        McpCallToolResp resp = new McpCallToolResp().setIsError(false);
        assertNotNull(resp.toString());
    }
}
