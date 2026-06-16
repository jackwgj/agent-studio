/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeTypeTest {

    @Test
    void testValues() {
        NodeType[] values = NodeType.values();
        assertTrue(values.length > 0);
    }

    @Test
    void testValueOf() {
        assertEquals(NodeType.START, NodeType.valueOf("START"));
        assertEquals(NodeType.END, NodeType.valueOf("END"));
        assertEquals(NodeType.LLM, NodeType.valueOf("LLM"));
    }

    @Test
    void testFromType_Existing() {
        assertEquals(NodeType.START, NodeType.fromType("Start"));
        assertEquals(NodeType.LLM, NodeType.fromType("LLM"));
        assertEquals(NodeType.CODE, NodeType.fromType("Code"));
    }

    @Test
    void testFromType_CaseInsensitive() {
        assertEquals(NodeType.START, NodeType.fromType("start"));
        assertEquals(NodeType.LLM, NodeType.fromType("llm"));
    }

    @Test
    void testFromType_NonExisting() {
        assertNull(NodeType.fromType("NonExistent"));
    }

    @Test
    void testFromJiuwen_Existing() {
        assertEquals(NodeType.START, NodeType.fromJiuwen("jiuwen.start"));
        assertEquals(NodeType.LLM, NodeType.fromJiuwen("jiuwen.LLMComponent"));
    }

    @Test
    void testFromJiuwen_NonExisting() {
        assertEquals(NodeType.UN_KNOWN, NodeType.fromJiuwen("non.existent"));
    }

    @Test
    void testFromJiuwen_Null() {
        assertEquals(NodeType.UN_KNOWN, NodeType.fromJiuwen(null));
    }

    @Test
    void testFromInsight_Existing() {
        assertEquals(NodeType.START, NodeType.fromInsight("Start"));
        assertEquals(NodeType.LLM, NodeType.fromInsight("LLM"));
    }

    @Test
    void testFromInsight_NonExisting() {
        assertEquals(NodeType.UN_KNOWN, NodeType.fromInsight("nonexistent"));
    }

    @Test
    void testFromDifyValue_Existing() {
        assertEquals(NodeType.START, NodeType.fromDifyValue("start"));
        assertEquals(NodeType.CODE, NodeType.fromDifyValue("code"));
    }

    @Test
    void testFromDifyValue_NonExisting() {
        assertEquals(NodeType.EMPTY, NodeType.fromDifyValue("nonexistent"));
    }

    @Test
    void testToInsight_Existing() {
        assertEquals("Start", NodeType.toInsight("Start"));
    }

    @Test
    void testToInsight_NonExisting() {
        assertNull(NodeType.toInsight("NonExistent"));
    }

    @Test
    void testGetEiType() {
        assertEquals("Start", NodeType.START.getEiType());
        assertEquals("LLM", NodeType.LLM.getEiType());
    }

    @Test
    void testGetJiuwenType() {
        assertEquals("jiuwen.start", NodeType.START.getJiuwenType());
        assertNull(NodeType.KNOWLEDGE.getJiuwenType());
    }

    @Test
    void testIsInnerNode() {
        assertFalse(NodeType.START.isInnerNode());
        assertTrue(NodeType.ENV.isInnerNode());
    }

    @Test
    void testGetDifyType() {
        assertEquals("start", NodeType.START.getDifyType());
        assertNull(NodeType.KNOWLEDGE.getDifyType());
    }
}
