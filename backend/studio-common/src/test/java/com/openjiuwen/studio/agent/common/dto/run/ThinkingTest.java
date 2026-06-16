/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ThinkingTest {

    @Test
    void testNoArgsConstructor() {
        Thinking thinking = new Thinking();
        assertNotNull(thinking);
        assertNull(thinking.getType());
    }

    @Test
    void testAllArgsConstructor() {
        Thinking thinking = new Thinking("enabled");
        assertEquals("enabled", thinking.getType());
    }

    @Test
    void testSetGetType() {
        Thinking thinking = new Thinking();
        thinking.setType("disabled");
        assertEquals("disabled", thinking.getType());
    }
}
