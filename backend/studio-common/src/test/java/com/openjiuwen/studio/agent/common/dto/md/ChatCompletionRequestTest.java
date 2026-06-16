/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.agent.common.dto.run.ChatMessage;
import com.openjiuwen.studio.agent.common.dto.run.Thinking;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChatCompletionRequestTest {

    @Test
    void testInheritedFields() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getModel());
        assertNull(req.getRefresh());
        req.setModel("gpt-4");
        req.setRefresh("true");
        assertEquals("gpt-4", req.getModel());
        assertEquals("true", req.getRefresh());
    }

    @Test
    void testSetGetMessages() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getMessages());
        List<ChatMessage> messages = List.of(new ChatMessage());
        req.setMessages(messages);
        assertEquals(messages, req.getMessages());
    }

    @Test
    void testSetGetStream() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getStream());
        req.setStream(true);
        assertEquals(true, req.getStream());
    }

    @Test
    void testSetGetStreamOptions() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getStreamOptions());
        req.setStreamOptions("options");
        assertEquals("options", req.getStreamOptions());
    }

    @Test
    void testSetGetMaxTokens() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getMaxTokens());
        req.setMaxTokens(1024);
        assertEquals(1024, req.getMaxTokens());
    }

    @Test
    void testSetGetTopK() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getTopK());
        req.setTopK(50);
        assertEquals(50, req.getTopK());
    }

    @Test
    void testSetGetTopP() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getTopP());
        req.setTopP(0.9);
        assertEquals(0.9, req.getTopP());
    }

    @Test
    void testSetGetTemperature() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getTemperature());
        req.setTemperature(0.7);
        assertEquals(0.7, req.getTemperature());
    }

    @Test
    void testSetGetStop() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getStop());
        req.setStop("END");
        assertEquals("END", req.getStop());
    }

    @Test
    void testSetGetN() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getN());
        req.setN(3);
        assertEquals(3, req.getN());
    }

    @Test
    void testSetGetUseBeamSearch() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getUseBeamSearch());
        req.setUseBeamSearch(true);
        assertEquals(true, req.getUseBeamSearch());
    }

    @Test
    void testSetGetPresencePenalty() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getPresencePenalty());
        req.setPresencePenalty(0.5f);
        assertEquals(0.5f, req.getPresencePenalty());
    }

    @Test
    void testSetGetFrequencyPenalty() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getFrequencyPenalty());
        req.setFrequencyPenalty(0.3f);
        assertEquals(0.3f, req.getFrequencyPenalty());
    }

    @Test
    void testSetGetLengthPenalty() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getLengthPenalty());
        req.setLengthPenalty(1.2f);
        assertEquals(1.2f, req.getLengthPenalty());
    }

    @Test
    void testSetGetIgnoreEos() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getIgnoreEos());
        req.setIgnoreEos(false);
        assertEquals(false, req.getIgnoreEos());
    }

    @Test
    void testSetGetLogitBias() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getLogitBias());
        Map<String, Integer> bias = new HashMap<>();
        bias.put("token1", 5);
        req.setLogitBias(bias);
        assertEquals(bias, req.getLogitBias());
    }

    @Test
    void testSetGetUser() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getUser());
        req.setUser("user-001");
        assertEquals("user-001", req.getUser());
    }

    @Test
    void testSetGetToolChoice() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getToolChoice());
        req.setToolChoice("auto");
        assertEquals("auto", req.getToolChoice());
    }

    @Test
    void testSetGetTools() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getTools());
        List<Object> tools = List.of("tool1");
        req.setTools(tools);
        assertEquals(tools, req.getTools());
    }

    @Test
    void testSetGetData() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getData());
        req.setData("data");
        assertEquals("data", req.getData());
    }

    @Test
    void testSetGetWithPrompt() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getWithPrompt());
        req.setWithPrompt(true);
        assertEquals(true, req.getWithPrompt());
    }

    @Test
    void testSetGetDecodeStrategy() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getDecodeStrategy());
        req.setDecodeStrategy("greedy");
        assertEquals("greedy", req.getDecodeStrategy());
    }

    @Test
    void testSetGetUserId() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getUserId());
        req.setUserId("uid-001");
        assertEquals("uid-001", req.getUserId());
    }

    @Test
    void testSetGetScenarioUuid() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getScenarioUuid());
        req.setScenarioUuid("uuid-001");
        assertEquals("uuid-001", req.getScenarioUuid());
    }

    @Test
    void testSetGetExtraInfo() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getExtraInfo());
        Map<String, Object> extra = new HashMap<>();
        extra.put("key", "value");
        req.setExtraInfo(extra);
        assertEquals(extra, req.getExtraInfo());
    }

    @Test
    void testSetGetThinking() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getThinking());
        Thinking thinking = new Thinking("enabled");
        req.setThinking(thinking);
        assertEquals(thinking, req.getThinking());
    }

    @Test
    void testSetGetEnableThinking() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getEnableThinking());
        req.setEnableThinking(true);
        assertEquals(true, req.getEnableThinking());
    }

    @Test
    void testSetGetChatTemplateKwargs() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNull(req.getChatTemplateKwargs());
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("key", "value");
        req.setChatTemplateKwargs(kwargs);
        assertEquals(kwargs, req.getChatTemplateKwargs());
    }

    @Test
    void testNoArgsConstructor() {
        ChatCompletionRequest req = new ChatCompletionRequest();
        assertNotNull(req);
    }
}
