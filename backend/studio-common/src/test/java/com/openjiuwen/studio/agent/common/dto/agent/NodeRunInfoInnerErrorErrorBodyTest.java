/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeRunInfoInnerErrorErrorBodyTest {

    @Test
    void testSetGetErrorCode() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody();
        assertNull(body.getErrorCode());
        NodeRunInfoInnerErrorErrorBody result = body.setErrorCode("ERR001");
        assertSame(body, result);
        assertEquals("ERR001", body.getErrorCode());
    }

    @Test
    void testSetGetErrorMessage() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody();
        assertNull(body.getErrorMessage());
        NodeRunInfoInnerErrorErrorBody result = body.setErrorMessage("something failed");
        assertSame(body, result);
        assertEquals("something failed", body.getErrorMessage());
    }

    @Test
    void testChainedSetters() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody()
                .setErrorCode("ERR001")
                .setErrorMessage("something failed");
        assertEquals("ERR001", body.getErrorCode());
        assertEquals("something failed", body.getErrorMessage());
    }

    @Test
    void testEquals_SameInstance() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody();
        assertEquals(body, body);
    }

    @Test
    void testEquals_EqualObjects() {
        NodeRunInfoInnerErrorErrorBody b1 = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E1").setErrorMessage("msg");
        NodeRunInfoInnerErrorErrorBody b2 = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E1").setErrorMessage("msg");
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        NodeRunInfoInnerErrorErrorBody b1 = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E1");
        NodeRunInfoInnerErrorErrorBody b2 = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E2");
        assertNotEquals(b1, b2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody();
        assertNotEquals(null, body);
        assertNotEquals("string", body);
    }

    @Test
    void testHashCode_Consistency() {
        NodeRunInfoInnerErrorErrorBody b1 = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E1").setErrorMessage("msg");
        NodeRunInfoInnerErrorErrorBody b2 = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E1").setErrorMessage("msg");
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void testToString() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody().setErrorCode("ERR001").setErrorMessage("fail");
        String str = body.toString();
        assertNotNull(str);
        assertTrue(str.contains("NodeRunInfoInnerErrorErrorBody"));
        assertTrue(str.contains("ERR001"));
        assertTrue(str.contains("fail"));
    }

    @Test
    void testToString_NullFields() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody();
        String str = body.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
