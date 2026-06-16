/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeRunInfoInnerErrorTest {

    @Test
    void testSetGetIsSuccess() {
        NodeRunInfoInnerError error = new NodeRunInfoInnerError();
        assertNull(error.isIsSuccess());
        NodeRunInfoInnerError result = error.setIsSuccess(true);
        assertSame(error, result);
        assertTrue(error.isIsSuccess());
    }

    @Test
    void testSetGetErrorBody() {
        NodeRunInfoInnerError error = new NodeRunInfoInnerError();
        assertNull(error.getErrorBody());
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody();
        NodeRunInfoInnerError result = error.setErrorBody(body);
        assertSame(error, result);
        assertEquals(body, error.getErrorBody());
    }

    @Test
    void testChainedSetters() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody()
                .setErrorCode("ERR001").setErrorMessage("something failed");
        NodeRunInfoInnerError error = new NodeRunInfoInnerError()
                .setIsSuccess(false)
                .setErrorBody(body);
        assertEquals(false, error.isIsSuccess());
        assertEquals("ERR001", error.getErrorBody().getErrorCode());
    }

    @Test
    void testEquals_SameInstance() {
        NodeRunInfoInnerError error = new NodeRunInfoInnerError();
        assertEquals(error, error);
    }

    @Test
    void testEquals_EqualObjects() {
        NodeRunInfoInnerErrorErrorBody body = new NodeRunInfoInnerErrorErrorBody().setErrorCode("E1");
        NodeRunInfoInnerError e1 = new NodeRunInfoInnerError().setIsSuccess(false).setErrorBody(body);
        NodeRunInfoInnerError e2 = new NodeRunInfoInnerError().setIsSuccess(false).setErrorBody(body);
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        NodeRunInfoInnerError e1 = new NodeRunInfoInnerError().setIsSuccess(true);
        NodeRunInfoInnerError e2 = new NodeRunInfoInnerError().setIsSuccess(false);
        assertNotEquals(e1, e2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        NodeRunInfoInnerError error = new NodeRunInfoInnerError();
        assertNotEquals(null, error);
        assertNotEquals("string", error);
    }

    @Test
    void testHashCode_Consistency() {
        NodeRunInfoInnerError e1 = new NodeRunInfoInnerError().setIsSuccess(true);
        NodeRunInfoInnerError e2 = new NodeRunInfoInnerError().setIsSuccess(true);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testToString() {
        NodeRunInfoInnerError error = new NodeRunInfoInnerError().setIsSuccess(false);
        String str = error.toString();
        assertNotNull(str);
        assertTrue(str.contains("NodeRunInfoInnerError"));
        assertTrue(str.contains("false"));
    }

    @Test
    void testToString_NullFields() {
        NodeRunInfoInnerError error = new NodeRunInfoInnerError();
        String str = error.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
