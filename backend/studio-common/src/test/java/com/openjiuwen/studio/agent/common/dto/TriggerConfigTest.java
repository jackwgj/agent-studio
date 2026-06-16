/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

class TriggerConfigTest {

    @Test
    void testAllGettersSetters() {
        List<TriggerParam> params = List.of(new TriggerParam().setName("p1").setDescription("d1"));
        Date createdOn = new Date();
        TriggerConfig obj = new TriggerConfig()
                .setTriggerId("trig-001")
                .setName("trigger1")
                .setType("cron")
                .setCron("0 0 * * *")
                .setPrompt("do something")
                .setInvocation("invoke-001")
                .setParams(params)
                .setHookUrl("http://example.com/hook")
                .setCreatedOn(createdOn);
        assertEquals("trig-001", obj.getTriggerId());
        assertEquals("trigger1", obj.getName());
        assertEquals("cron", obj.getType());
        assertEquals("0 0 * * *", obj.getCron());
        assertEquals("do something", obj.getPrompt());
        assertEquals("invoke-001", obj.getInvocation());
        assertEquals(params, obj.getParams());
        assertEquals("http://example.com/hook", obj.getHookUrl());
        assertEquals(createdOn, obj.getCreatedOn());
    }

    @Test
    void testEquals_SameObject() {
        TriggerConfig obj = new TriggerConfig().setTriggerId("trig-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        Date date = new Date(1000000L);
        TriggerConfig obj1 = new TriggerConfig()
                .setTriggerId("trig-001").setName("trigger1").setType("cron")
                .setCron("0 0 * * *").setPrompt("prompt").setInvocation("inv")
                .setHookUrl("http://hook").setCreatedOn(date);
        TriggerConfig obj2 = new TriggerConfig()
                .setTriggerId("trig-001").setName("trigger1").setType("cron")
                .setCron("0 0 * * *").setPrompt("prompt").setInvocation("inv")
                .setHookUrl("http://hook").setCreatedOn(date);
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        TriggerConfig obj = new TriggerConfig().setTriggerId("trig-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        TriggerConfig obj = new TriggerConfig().setTriggerId("trig-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        TriggerConfig obj1 = new TriggerConfig().setTriggerId("trig-001");
        TriggerConfig obj2 = new TriggerConfig().setTriggerId("trig-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        TriggerConfig obj1 = new TriggerConfig().setTriggerId("trig-001").setName("trigger1");
        TriggerConfig obj2 = new TriggerConfig().setTriggerId("trig-001").setName("trigger1");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        TriggerConfig obj = new TriggerConfig().setTriggerId("trig-001");
        assertNotNull(obj.toString());
    }
}
