/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

class WorkflowListRspTest {

    @Test
    void testSetGetCount() {
        WorkflowListRsp rsp = new WorkflowListRsp();
        assertNull(rsp.getCount());
        WorkflowListRsp result = rsp.setCount(10L);
        assertSame(rsp, result);
        assertEquals(10L, rsp.getCount());
    }

    @Test
    void testSetGetWorkflowList() {
        WorkflowListRsp rsp = new WorkflowListRsp();
        assertNull(rsp.getWorkflowList());
        List<WorkflowListItem> items = List.of(new WorkflowListItem().setWorkflowId("wf-001"));
        WorkflowListRsp result = rsp.setWorkflowList(items);
        assertSame(rsp, result);
        assertEquals(items, rsp.getWorkflowList());
    }

    @Test
    void testChainedSetters() {
        WorkflowListRsp rsp = new WorkflowListRsp()
                .setCount(2L)
                .setWorkflowList(List.of(new WorkflowListItem().setName("wf1"), new WorkflowListItem().setName("wf2")));
        assertEquals(2L, rsp.getCount());
        assertEquals(2, rsp.getWorkflowList().size());
    }

    @Test
    void testEquals_SameInstance() {
        WorkflowListRsp rsp = new WorkflowListRsp();
        assertEquals(rsp, rsp);
    }

    @Test
    void testEquals_EqualObjects() {
        WorkflowListRsp r1 = new WorkflowListRsp().setCount(5L);
        WorkflowListRsp r2 = new WorkflowListRsp().setCount(5L);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        WorkflowListRsp r1 = new WorkflowListRsp().setCount(5L);
        WorkflowListRsp r2 = new WorkflowListRsp().setCount(10L);
        assertNotEquals(r1, r2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        WorkflowListRsp rsp = new WorkflowListRsp();
        assertNotEquals(null, rsp);
        assertNotEquals("string", rsp);
    }

    @Test
    void testHashCode_Consistency() {
        WorkflowListRsp r1 = new WorkflowListRsp().setCount(5L);
        WorkflowListRsp r2 = new WorkflowListRsp().setCount(5L);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        WorkflowListRsp rsp = new WorkflowListRsp().setCount(5L);
        String str = rsp.toString();
        assertNotNull(str);
        assertTrue(str.contains("WorkflowListRsp"));
        assertTrue(str.contains("5"));
    }

    @Test
    void testToString_NullFields() {
        WorkflowListRsp rsp = new WorkflowListRsp();
        String str = rsp.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
