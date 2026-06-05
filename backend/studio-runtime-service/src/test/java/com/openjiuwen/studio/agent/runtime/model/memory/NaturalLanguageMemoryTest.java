/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NaturalLanguageMemoryTest {

    @Test
    void test_NaturalLanguageMemory_equal() {

        NaturalLanguageMemory item1 = new NaturalLanguageMemory();
        item1.setMemoryId("id");
        item1.setOperation("UPDATE");
        item1.setContent("test");

        NaturalLanguageMemory item2 = new NaturalLanguageMemory();
        item2.setMemoryId("id");
        item2.setOperation("UPDATE");
        item2.setContent("test");

        assertTrue(item1.equals(item2));
    }

    @Test
    void test_NaturalLanguageMemory_not_equal() {

        NaturalLanguageMemory item1 = new NaturalLanguageMemory();
        item1.setMemoryId("id");
        item1.setOperation("UPDATE");
        item1.setContent("test");

        UserProfileTag item2 = new UserProfileTag();
        item2.setName("name");
        item2.setTopic("topic");
        item2.setValue("test");

        assertFalse(item1.equals(item2));
    }

    @Test
    void test_NaturalLanguageMemory_hash() {

        NaturalLanguageMemory item1 = new NaturalLanguageMemory();
        item1.setMemoryId("id");
        item1.setOperation("UPDATE");
        item1.setContent("test");

        assertTrue(item1.hashCode() != 0);
    }
}
