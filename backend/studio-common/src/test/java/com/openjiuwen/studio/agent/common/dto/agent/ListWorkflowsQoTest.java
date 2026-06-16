/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ListWorkflowsQoTest {

    @Test
    void testAllGettersSetters() {
        ListWorkflowsQo obj = new ListWorkflowsQo()
                .setWorkspaceId("ws-001")
                .setOffset(10)
                .setLimit(20)
                .setId("id-001")
                .setName("workflow1")
                .setCode("code-001")
                .setDescription("desc")
                .setCreator("creator1")
                .setGroup("group1")
                .setType("type1")
                .setCustomizeNode(true)
                .setStatus("active");
        assertEquals("ws-001", obj.getWorkspaceId());
        assertEquals(10, obj.getOffset());
        assertEquals(20, obj.getLimit());
        assertEquals("id-001", obj.getId());
        assertEquals("workflow1", obj.getName());
        assertEquals("code-001", obj.getCode());
        assertEquals("desc", obj.getDescription());
        assertEquals("creator1", obj.getCreator());
        assertEquals("group1", obj.getGroup());
        assertEquals("type1", obj.getType());
        assertTrue(obj.isCustomizeNode());
        assertEquals("active", obj.getStatus());
    }

    @Test
    void testEquals_SameObject() {
        ListWorkflowsQo obj = new ListWorkflowsQo().setWorkspaceId("ws-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        ListWorkflowsQo obj1 = new ListWorkflowsQo()
                .setWorkspaceId("ws-001").setOffset(10).setLimit(20)
                .setId("id-001").setName("workflow1").setCode("code-001")
                .setDescription("desc").setCreator("creator1").setGroup("group1")
                .setType("type1").setCustomizeNode(true).setStatus("active");
        ListWorkflowsQo obj2 = new ListWorkflowsQo()
                .setWorkspaceId("ws-001").setOffset(10).setLimit(20)
                .setId("id-001").setName("workflow1").setCode("code-001")
                .setDescription("desc").setCreator("creator1").setGroup("group1")
                .setType("type1").setCustomizeNode(true).setStatus("active");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        ListWorkflowsQo obj = new ListWorkflowsQo().setWorkspaceId("ws-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        ListWorkflowsQo obj = new ListWorkflowsQo().setWorkspaceId("ws-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        ListWorkflowsQo obj1 = new ListWorkflowsQo().setWorkspaceId("ws-001");
        ListWorkflowsQo obj2 = new ListWorkflowsQo().setWorkspaceId("ws-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        ListWorkflowsQo obj1 = new ListWorkflowsQo()
                .setWorkspaceId("ws-001").setOffset(10).setLimit(20);
        ListWorkflowsQo obj2 = new ListWorkflowsQo()
                .setWorkspaceId("ws-001").setOffset(10).setLimit(20);
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ListWorkflowsQo obj = new ListWorkflowsQo().setWorkspaceId("ws-001");
        assertNotNull(obj.toString());
    }
}
