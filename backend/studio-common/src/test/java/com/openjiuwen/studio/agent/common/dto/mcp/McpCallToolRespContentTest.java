/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class McpCallToolRespContentTest {

    @Test
    void testGettersAndSetters_AllFields() {
        McpCallToolRespContent content = new McpCallToolRespContent()
                .setType("text")
                .setText("hello world");

        assertEquals("text", content.getType());
        assertEquals("hello world", content.getText());
    }

    @Test
    void testSetters_ReturnThis() {
        McpCallToolRespContent content = new McpCallToolRespContent();
        assertSame(content, content.setType("type"));
        assertSame(content, content.setText("text"));
    }

    @Test
    void testEquals_SameObject() {
        McpCallToolRespContent content = new McpCallToolRespContent().setType("text");
        assertEquals(content, content);
    }

    @Test
    void testEquals_EqualObject() {
        McpCallToolRespContent c1 = new McpCallToolRespContent().setType("text").setText("hello");
        McpCallToolRespContent c2 = new McpCallToolRespContent().setType("text").setText("hello");
        assertEquals(c1, c2);
    }

    @Test
    void testEquals_Null() {
        McpCallToolRespContent content = new McpCallToolRespContent();
        assertNotEquals(null, content);
    }

    @Test
    void testEquals_DifferentClass() {
        McpCallToolRespContent content = new McpCallToolRespContent();
        assertNotEquals("string", content);
    }

    @Test
    void testHashCode_Consistency() {
        McpCallToolRespContent c1 = new McpCallToolRespContent().setType("text").setText("hello");
        McpCallToolRespContent c2 = new McpCallToolRespContent().setType("text").setText("hello");
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        McpCallToolRespContent content = new McpCallToolRespContent().setType("text");
        assertNotNull(content.toString());
    }
}
