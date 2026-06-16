/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LakeSearchQueryScopeEnumTest {

    @Test
    void testEnumValues() {
        LakeSearchQueryScopeEnum[] values = LakeSearchQueryScopeEnum.values();
        assertEquals(4, values.length);
    }

    @Test
    void testDoc() {
        assertEquals("doc", LakeSearchQueryScopeEnum.DOC.getName());
    }

    @Test
    void testKeyword() {
        assertEquals("keyword", LakeSearchQueryScopeEnum.KEYWORD.getName());
    }

    @Test
    void testMix() {
        assertEquals("mix", LakeSearchQueryScopeEnum.MIX.getName());
    }

    @Test
    void testFaq() {
        assertEquals("faq", LakeSearchQueryScopeEnum.FAQ.getName());
    }

    @Test
    void testValueOf() {
        assertEquals(LakeSearchQueryScopeEnum.DOC, LakeSearchQueryScopeEnum.valueOf("DOC"));
        assertEquals(LakeSearchQueryScopeEnum.KEYWORD, LakeSearchQueryScopeEnum.valueOf("KEYWORD"));
        assertEquals(LakeSearchQueryScopeEnum.MIX, LakeSearchQueryScopeEnum.valueOf("MIX"));
        assertEquals(LakeSearchQueryScopeEnum.FAQ, LakeSearchQueryScopeEnum.valueOf("FAQ"));
    }
}
