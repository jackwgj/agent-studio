/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StatusTest {

    @Test
    void testSetGetCode() {
        Status status = new Status();
        assertNull(status.getCode());
        Status result = status.setCode(200);
        assertSame(status, result);
        assertEquals(200, status.getCode());
    }

    @Test
    void testSetGetDesc() {
        Status status = new Status();
        assertNull(status.getDesc());
        Status result = status.setDesc("success");
        assertSame(status, result);
        assertEquals("success", status.getDesc());
    }

    @Test
    void testChainedSetters() {
        Status status = new Status().setCode(404).setDesc("not found");
        assertEquals(404, status.getCode());
        assertEquals("not found", status.getDesc());
    }

    @Test
    void testEquals_SameInstance() {
        Status status = new Status().setCode(200);
        assertEquals(status, status);
    }

    @Test
    void testEquals_EqualObjects() {
        Status s1 = new Status().setCode(200).setDesc("OK");
        Status s2 = new Status().setCode(200).setDesc("OK");
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        Status s1 = new Status().setCode(200);
        Status s2 = new Status().setCode(404);
        assertNotEquals(s1, s2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        Status status = new Status().setCode(200);
        assertNotEquals(null, status);
        assertNotEquals("string", status);
    }

    @Test
    void testHashCode_Consistency() {
        Status s1 = new Status().setCode(200).setDesc("OK");
        Status s2 = new Status().setCode(200).setDesc("OK");
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testToString() {
        Status status = new Status().setCode(200).setDesc("OK");
        String str = status.toString();
        assertNotNull(str);
        assertTrue(str.contains("Status"));
        assertTrue(str.contains("200"));
        assertTrue(str.contains("OK"));
    }

    @Test
    void testToString_NullFields() {
        Status status = new Status();
        String str = status.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
