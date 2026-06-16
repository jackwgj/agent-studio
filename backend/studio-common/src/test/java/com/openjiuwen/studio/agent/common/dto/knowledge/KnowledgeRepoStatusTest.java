/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeRepoStatusTest {

    @Test
    void testEnumValues() {
        KnowledgeRepoStatus[] values = KnowledgeRepoStatus.values();
        assertEquals(2, values.length);
    }

    @Test
    void testOpen() {
        assertEquals("OPEN", KnowledgeRepoStatus.OPEN.toString());
    }

    @Test
    void testClose() {
        assertEquals("CLOSE", KnowledgeRepoStatus.CLOSE.toString());
    }

    @Test
    void testFromValue_ValidOpen() {
        assertEquals(KnowledgeRepoStatus.OPEN, KnowledgeRepoStatus.fromValue("OPEN"));
    }

    @Test
    void testFromValue_ValidClose() {
        assertEquals(KnowledgeRepoStatus.CLOSE, KnowledgeRepoStatus.fromValue("CLOSE"));
    }

    @Test
    void testFromValue_CaseInsensitive() {
        assertEquals(KnowledgeRepoStatus.OPEN, KnowledgeRepoStatus.fromValue("open"));
    }

    @Test
    void testFromValue_Invalid() {
        assertNull(KnowledgeRepoStatus.fromValue("INVALID"));
    }

    @Test
    void testValueOf() {
        assertEquals(KnowledgeRepoStatus.OPEN, KnowledgeRepoStatus.valueOf("OPEN"));
        assertEquals(KnowledgeRepoStatus.CLOSE, KnowledgeRepoStatus.valueOf("CLOSE"));
    }
}
