/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongTermMemoryTriggerTypeTest {

    @Test
    void testValues() {
        assertEquals(1, LongTermMemoryTriggerType.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(LongTermMemoryTriggerType.SEARCH, LongTermMemoryTriggerType.valueOf("SEARCH"));
    }

    @Test
    void testToString() {
        assertEquals("search", LongTermMemoryTriggerType.SEARCH.toString());
    }
}
