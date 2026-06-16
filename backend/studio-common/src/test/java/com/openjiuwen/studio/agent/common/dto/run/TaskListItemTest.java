/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Date;

class TaskListItemTest {

    @Test
    void testAllGettersSetters() {
        Date createTime = new Date(1000000L);
        Date finishTime = new Date(2000000L);
        TaskListItem obj = new TaskListItem()
                .setId("task-001")
                .setName("task1")
                .setType("batch")
                .setIsPublished(true)
                .setStatus(TaskStatus.COMPLETED)
                .setCreateTime(createTime)
                .setFinishTime(finishTime);
        assertEquals("task-001", obj.getId());
        assertEquals("task1", obj.getName());
        assertEquals("batch", obj.getType());
        assertTrue(obj.isIsPublished());
        assertEquals(TaskStatus.COMPLETED, obj.getStatus());
        assertEquals(createTime, obj.getCreateTime());
        assertEquals(finishTime, obj.getFinishTime());
    }

    @Test
    void testEquals_SameObject() {
        TaskListItem obj = new TaskListItem().setId("task-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        Date createTime = new Date(1000000L);
        Date finishTime = new Date(2000000L);
        TaskListItem obj1 = new TaskListItem()
                .setId("task-001").setName("task1").setType("batch")
                .setIsPublished(true).setStatus(TaskStatus.COMPLETED)
                .setCreateTime(createTime).setFinishTime(finishTime);
        TaskListItem obj2 = new TaskListItem()
                .setId("task-001").setName("task1").setType("batch")
                .setIsPublished(true).setStatus(TaskStatus.COMPLETED)
                .setCreateTime(createTime).setFinishTime(finishTime);
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        TaskListItem obj = new TaskListItem().setId("task-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        TaskListItem obj = new TaskListItem().setId("task-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        TaskListItem obj1 = new TaskListItem().setId("task-001");
        TaskListItem obj2 = new TaskListItem().setId("task-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        TaskListItem obj1 = new TaskListItem().setId("task-001").setName("task1");
        TaskListItem obj2 = new TaskListItem().setId("task-001").setName("task1");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        TaskListItem obj = new TaskListItem().setId("task-001");
        assertNotNull(obj.toString());
    }
}
