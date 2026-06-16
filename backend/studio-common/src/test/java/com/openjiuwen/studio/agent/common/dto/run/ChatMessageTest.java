/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import java.util.List;

class ChatMessageTest {

    @Test
    void testSetGetRole() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getRole());
        msg.setRole("user");
        assertEquals("user", msg.getRole());
    }

    @Test
    void testSetGetContent() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getContent());
        msg.setContent("hello");
        assertEquals("hello", msg.getContent());
    }

    @Test
    void testSetGetReasoningContent() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getReasoningContent());
        msg.setReasoningContent("thinking...");
        assertEquals("thinking...", msg.getReasoningContent());
    }

    @Test
    void testSetGetName() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getName());
        msg.setName("testUser");
        assertEquals("testUser", msg.getName());
    }

    @Test
    void testSetGetToolCalls() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getToolCalls());
        List<Object> toolCalls = List.of("call1");
        msg.setToolCalls(toolCalls);
        assertEquals(toolCalls, msg.getToolCalls());
    }

    @Test
    void testSetGetToolCallId() {
        ChatMessage msg = new ChatMessage();
        assertNull(msg.getToolCallId());
        msg.setToolCallId("tc-001");
        assertEquals("tc-001", msg.getToolCallId());
    }

    @Test
    void testChainedSetters() {
        ChatMessage msg = new ChatMessage();
        ChatMessage result = msg.setRole("assistant").setContent("response").setName("bot");
        assertSame(msg, result);
        assertEquals("assistant", msg.getRole());
        assertEquals("response", msg.getContent());
        assertEquals("bot", msg.getName());
    }

    @Test
    void testNoArgsConstructor() {
        ChatMessage msg = new ChatMessage();
        assertNotNull(msg);
        assertNull(msg.getRole());
        assertNull(msg.getContent());
    }
}
