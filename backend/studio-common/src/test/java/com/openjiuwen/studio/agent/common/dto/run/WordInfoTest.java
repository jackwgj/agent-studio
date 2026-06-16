/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class WordInfoTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        WordInfo info = new WordInfo()
                .setStartTime(100)
                .setEndTime(200)
                .setWord("hello");
        assertEquals(100, info.getStartTime());
        assertEquals(200, info.getEndTime());
        assertEquals("hello", info.getWord());
    }

    @Test
    void testEquals_SameInstance() {
        WordInfo info = new WordInfo().setWord("w");
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObjects() {
        WordInfo w1 = new WordInfo().setStartTime(1).setEndTime(2).setWord("w");
        WordInfo w2 = new WordInfo().setStartTime(1).setEndTime(2).setWord("w");
        assertEquals(w1, w2);
    }

    @Test
    void testEquals_Null() {
        WordInfo info = new WordInfo();
        assertNotEquals(null, info);
    }

    @Test
    void testEquals_DifferentClass() {
        WordInfo info = new WordInfo();
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        WordInfo w1 = new WordInfo().setStartTime(1).setEndTime(2).setWord("w");
        WordInfo w2 = new WordInfo().setStartTime(1).setEndTime(2).setWord("w");
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        WordInfo info = new WordInfo().setWord("hello");
        assertNotNull(info.toString());
    }
}
