/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TriggerParamTest {

    @Test
    void testGettersAndSetters_AllFields() {
        TriggerParam param = new TriggerParam()
                .setName("trigger1")
                .setDescription("a trigger");

        assertEquals("trigger1", param.getName());
        assertEquals("a trigger", param.getDescription());
    }

    @Test
    void testSetters_ReturnThis() {
        TriggerParam param = new TriggerParam();
        assertSame(param, param.setName("name"));
        assertSame(param, param.setDescription("desc"));
    }

    @Test
    void testEquals_SameObject() {
        TriggerParam param = new TriggerParam().setName("name");
        assertEquals(param, param);
    }

    @Test
    void testEquals_EqualObject() {
        TriggerParam p1 = new TriggerParam().setName("name").setDescription("desc");
        TriggerParam p2 = new TriggerParam().setName("name").setDescription("desc");
        assertEquals(p1, p2);
    }

    @Test
    void testEquals_Null() {
        TriggerParam param = new TriggerParam();
        assertNotEquals(null, param);
    }

    @Test
    void testEquals_DifferentClass() {
        TriggerParam param = new TriggerParam();
        assertNotEquals("string", param);
    }

    @Test
    void testHashCode_Consistency() {
        TriggerParam p1 = new TriggerParam().setName("name").setDescription("desc");
        TriggerParam p2 = new TriggerParam().setName("name").setDescription("desc");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        TriggerParam param = new TriggerParam().setName("name");
        assertNotNull(param.toString());
    }
}
