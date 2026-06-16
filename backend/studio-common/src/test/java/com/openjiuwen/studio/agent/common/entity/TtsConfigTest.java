/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TtsConfigTest {

    @Test
    void testGettersAndSetters() {
        TtsConfig config = new TtsConfig();
        config.setAudioFormat("mp3");
        config.setSampleRate("16000");
        config.setProperty("property1");
        config.setSpeed(100);
        config.setVolume(80);

        assertEquals("mp3", config.getAudioFormat());
        assertEquals("16000", config.getSampleRate());
        assertEquals("property1", config.getProperty());
        assertEquals(100, config.getSpeed());
        assertEquals(80, config.getVolume());
    }

    @Test
    void testDefaultValues() {
        TtsConfig config = new TtsConfig();
        assertNull(config.getAudioFormat());
        assertNull(config.getSampleRate());
        assertNull(config.getProperty());
        assertNull(config.getSpeed());
        assertNull(config.getVolume());
    }

    @Test
    void testEqualsAndHashCode() {
        TtsConfig config1 = new TtsConfig();
        config1.setAudioFormat("mp3");
        config1.setSpeed(100);
        TtsConfig config2 = new TtsConfig();
        config2.setAudioFormat("mp3");
        config2.setSpeed(100);
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        TtsConfig config = new TtsConfig();
        config.setAudioFormat("mp3");
        assertNotNull(config.toString());
    }

    @Test
    void testSpeedBoundaryValues() {
        TtsConfig config = new TtsConfig();
        config.setSpeed(-500);
        assertEquals(-500, config.getSpeed());
        config.setSpeed(500);
        assertEquals(500, config.getSpeed());
    }

    @Test
    void testVolumeBoundaryValues() {
        TtsConfig config = new TtsConfig();
        config.setVolume(0);
        assertEquals(0, config.getVolume());
        config.setVolume(100);
        assertEquals(100, config.getVolume());
    }
}
