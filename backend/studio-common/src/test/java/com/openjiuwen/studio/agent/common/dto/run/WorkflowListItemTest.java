/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkflowListItemTest {

    @Test
    void testSetGetWorkflowId() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getWorkflowId());
        WorkflowListItem result = item.setWorkflowId("wf-001");
        assertSame(item, result);
        assertEquals("wf-001", item.getWorkflowId());
    }

    @Test
    void testSetGetName() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getName());
        WorkflowListItem result = item.setName("My Workflow");
        assertSame(item, result);
        assertEquals("My Workflow", item.getName());
    }

    @Test
    void testSetGetAvatar() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getAvatar());
        WorkflowListItem result = item.setAvatar("avatar.png");
        assertSame(item, result);
        assertEquals("avatar.png", item.getAvatar());
    }

    @Test
    void testSetGetCode() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getCode());
        WorkflowListItem result = item.setCode("WF001");
        assertSame(item, result);
        assertEquals("WF001", item.getCode());
    }

    @Test
    void testSetGetDescription() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getDescription());
        WorkflowListItem result = item.setDescription("A workflow");
        assertSame(item, result);
        assertEquals("A workflow", item.getDescription());
    }

    @Test
    void testSetGetCreator() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getCreator());
        WorkflowListItem result = item.setCreator("user1");
        assertSame(item, result);
        assertEquals("user1", item.getCreator());
    }

    @Test
    void testSetGetType() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getType());
        WorkflowListItem result = item.setType("standard");
        assertSame(item, result);
        assertEquals("standard", item.getType());
    }

    @Test
    void testSetGetGroup() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getGroup());
        WorkflowListItem result = item.setGroup("group1");
        assertSame(item, result);
        assertEquals("group1", item.getGroup());
    }

    @Test
    void testSetGetUrl() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getUrl());
        WorkflowListItem result = item.setUrl("http://example.com");
        assertSame(item, result);
        assertEquals("http://example.com", item.getUrl());
    }

    @Test
    void testSetGetStatus() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getStatus());
        WorkflowListItem result = item.setStatus(1);
        assertSame(item, result);
        assertEquals(1, item.getStatus());
    }

    @Test
    void testSetGetCustomizeNode() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.isCustomizeNode());
        WorkflowListItem result = item.setCustomizeNode(true);
        assertSame(item, result);
        assertTrue(item.isCustomizeNode());
    }

    @Test
    void testSetGetUpdatedOn() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getUpdatedOn());
        WorkflowListItem result = item.setUpdatedOn(1000L);
        assertSame(item, result);
        assertEquals(1000L, item.getUpdatedOn());
    }

    @Test
    void testSetGetLastVersionId() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getLastVersionId());
        WorkflowListItem result = item.setLastVersionId("v-001");
        assertSame(item, result);
        assertEquals("v-001", item.getLastVersionId());
    }

    @Test
    void testSetGetIsShare() {
        WorkflowListItem item = new WorkflowListItem();
        assertNull(item.getIsShare());
        WorkflowListItem result = item.setIsShare(1);
        assertSame(item, result);
        assertEquals(1, item.getIsShare());
    }

    @Test
    void testChainedSetters() {
        WorkflowListItem item = new WorkflowListItem()
                .setWorkflowId("wf-001")
                .setName("My Workflow")
                .setType("standard")
                .setStatus(1);
        assertEquals("wf-001", item.getWorkflowId());
        assertEquals("My Workflow", item.getName());
        assertEquals("standard", item.getType());
        assertEquals(1, item.getStatus());
    }

    @Test
    void testEquals_SameInstance() {
        WorkflowListItem item = new WorkflowListItem().setWorkflowId("wf-001");
        assertEquals(item, item);
    }

    @Test
    void testEquals_EqualObjects() {
        WorkflowListItem i1 = new WorkflowListItem().setWorkflowId("wf-001").setName("WF");
        WorkflowListItem i2 = new WorkflowListItem().setWorkflowId("wf-001").setName("WF");
        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        WorkflowListItem i1 = new WorkflowListItem().setWorkflowId("wf-001");
        WorkflowListItem i2 = new WorkflowListItem().setWorkflowId("wf-002");
        assertNotEquals(i1, i2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        WorkflowListItem item = new WorkflowListItem();
        assertNotEquals(null, item);
        assertNotEquals("string", item);
    }

    @Test
    void testHashCode_Consistency() {
        WorkflowListItem i1 = new WorkflowListItem().setWorkflowId("wf-001").setName("WF");
        WorkflowListItem i2 = new WorkflowListItem().setWorkflowId("wf-001").setName("WF");
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testToString() {
        WorkflowListItem item = new WorkflowListItem().setWorkflowId("wf-001").setName("WF");
        String str = item.toString();
        assertNotNull(str);
        assertTrue(str.contains("WorkflowListItem"));
        assertTrue(str.contains("wf-001"));
    }

    @Test
    void testToString_NullFields() {
        WorkflowListItem item = new WorkflowListItem();
        String str = item.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
