/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class Text2AudioReqTest {

    @Test
    void testGettersAndSetters() {
        Text2AudioReq req = new Text2AudioReq();
        req.setText("Hello World");
        TtsConfig config = new TtsConfig();
        config.setAudioFormat("mp3");
        req.setConfig(config);
        assertEquals("Hello World", req.getText());
        assertEquals("mp3", req.getConfig().getAudioFormat());
    }

    @Test
    void testDefaultValues() {
        Text2AudioReq req = new Text2AudioReq();
        assertNull(req.getText());
        assertNull(req.getConfig());
    }

    @Test
    void testEqualsAndHashCode() {
        Text2AudioReq req1 = new Text2AudioReq();
        req1.setText("text");
        Text2AudioReq req2 = new Text2AudioReq();
        req2.setText("text");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
    }

    @Test
    void testToString() {
        Text2AudioReq req = new Text2AudioReq();
        req.setText("test");
        assertNotNull(req.toString());
    }
}
