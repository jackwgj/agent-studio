/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

class MessageTest {

    @Test
    void testSetGetRole() {
        Message msg = new Message();
        assertNull(msg.getRole());
        Message result = msg.setRole("user");
        assertSame(msg, result);
        assertEquals("user", msg.getRole());
    }

    @Test
    void testSetGetContent() {
        Message msg = new Message();
        assertNull(msg.getContent());
        Message result = msg.setContent("hello");
        assertSame(msg, result);
        assertEquals("hello", msg.getContent());
    }

    @Test
    void testSetGetCreateTime() {
        Message msg = new Message();
        assertNull(msg.getCreateTime());
        Message result = msg.setCreateTime(1000L);
        assertSame(msg, result);
        assertEquals(1000L, msg.getCreateTime());
    }

    @Test
    void testSetGetName() {
        Message msg = new Message();
        assertNull(msg.getName());
        Message result = msg.setName("testName");
        assertSame(msg, result);
        assertEquals("testName", msg.getName());
    }

    @Test
    void testSetGetFunctionCall() {
        Message msg = new Message();
        assertNull(msg.getFunctionCall());
        Object fc = "functionCallObj";
        Message result = msg.setFunctionCall(fc);
        assertSame(msg, result);
        assertEquals(fc, msg.getFunctionCall());
    }

    @Test
    void testSetGetToolCalls() {
        Message msg = new Message();
        assertNull(msg.getToolCalls());
        Object tc = "toolCallsObj";
        Message result = msg.setToolCalls(tc);
        assertSame(msg, result);
        assertEquals(tc, msg.getToolCalls());
    }

    @Test
    void testSetGetToolCallId() {
        Message msg = new Message();
        assertNull(msg.getToolCallId());
        Object tci = "toolCallIdObj";
        Message result = msg.setToolCallId(tci);
        assertSame(msg, result);
        assertEquals(tci, msg.getToolCallId());
    }

    @Test
    void testSetGetEnableHistory() {
        Message msg = new Message();
        assertNull(msg.isEnableHistory());
        Message result = msg.setEnableHistory(true);
        assertSame(msg, result);
        assertTrue(msg.isEnableHistory());
    }

    @Test
    void testSetGetIntent() {
        Message msg = new Message();
        assertNull(msg.getIntent());
        List<String> intent = List.of("greeting", "question");
        Message result = msg.setIntent(intent);
        assertSame(msg, result);
        assertEquals(intent, msg.getIntent());
    }

    @Test
    void testSetGetExecutionId() {
        Message msg = new Message();
        assertNull(msg.getExecutionId());
        Message result = msg.setExecutionId("exec-001");
        assertSame(msg, result);
        assertEquals("exec-001", msg.getExecutionId());
    }

    @Test
    void testSetGetNodeId() {
        Message msg = new Message();
        assertNull(msg.getNodeId());
        Message result = msg.setNodeId("node-001");
        assertSame(msg, result);
        assertEquals("node-001", msg.getNodeId());
    }

    @Test
    void testSetGetAgentId() {
        Message msg = new Message();
        assertNull(msg.getAgentId());
        Message result = msg.setAgentId("agent-001");
        assertSame(msg, result);
        assertEquals("agent-001", msg.getAgentId());
    }

    @Test
    void testSetGetRating() {
        Message msg = new Message();
        assertNull(msg.getRating());
        Message result = msg.setRating(1);
        assertSame(msg, result);
        assertEquals(1, msg.getRating());
    }

    @Test
    void testSetGetFiles() {
        Message msg = new Message();
        assertNull(msg.getFiles());
        List<Object> files = List.of("file1", "file2");
        Message result = msg.setFiles(files);
        assertSame(msg, result);
        assertEquals(files, msg.getFiles());
    }

    @Test
    void testSetGetReason() {
        Message msg = new Message();
        assertNull(msg.getReason());
        FeedbackReason reason = new FeedbackReason().setContent("good");
        Message result = msg.setReason(reason);
        assertSame(msg, result);
        assertEquals(reason, msg.getReason());
    }

    @Test
    void testChainedSetters() {
        Message msg = new Message()
                .setRole("assistant")
                .setContent("response")
                .setCreateTime(1000L)
                .setAgentId("agent-001")
                .setNodeId("node-001")
                .setRating(1);
        assertEquals("assistant", msg.getRole());
        assertEquals("response", msg.getContent());
        assertEquals(1000L, msg.getCreateTime());
        assertEquals("agent-001", msg.getAgentId());
        assertEquals("node-001", msg.getNodeId());
        assertEquals(1, msg.getRating());
    }

    @Test
    void testEquals_SameInstance() {
        Message msg = new Message().setRole("user");
        assertEquals(msg, msg);
    }

    @Test
    void testEquals_EqualObjects() {
        Message msg1 = new Message().setRole("user").setContent("hello");
        Message msg2 = new Message().setRole("user").setContent("hello");
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        Message msg1 = new Message().setRole("user");
        Message msg2 = new Message().setRole("assistant");
        assertNotEquals(msg1, msg2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        Message msg = new Message().setRole("user");
        assertNotEquals(null, msg);
        assertNotEquals("string", msg);
    }

    @Test
    void testHashCode_Consistency() {
        Message msg1 = new Message().setRole("user").setContent("hello");
        Message msg2 = new Message().setRole("user").setContent("hello");
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testToString() {
        Message msg = new Message().setRole("user").setContent("hello");
        String str = msg.toString();
        assertNotNull(str);
        assertTrue(str.contains("Message"));
        assertTrue(str.contains("user"));
        assertTrue(str.contains("hello"));
    }

    @Test
    void testToString_NullFields() {
        Message msg = new Message();
        String str = msg.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }
}
