/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.sensitive;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveEntityTest {

    @Test
    void testSettersAndGetters() {
        SensitiveEntity entity = new SensitiveEntity();
        entity.setEnabled(true);
        assertTrue(entity.isEnabled());

        SensitiveEntity.WordsWrapper filter = new SensitiveEntity.WordsWrapper();
        entity.setFilter(filter);
        assertEquals(filter, entity.getFilter());

        SensitiveEntity.WordsWrapper replaceWrapper = new SensitiveEntity.WordsWrapper();
        entity.setReplace(List.of(replaceWrapper));
        assertEquals(1, entity.getReplace().size());

        SensitiveEntity.WordsWrapper replyWrapper = new SensitiveEntity.WordsWrapper();
        entity.setReply(List.of(replyWrapper));
        assertEquals(1, entity.getReply().size());
    }

    @Test
    void testWordsWrapper_GetKeywordsList_Valid() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords("word1,word2,word3");
        List<String> keywords = wrapper.getKeywordsList();
        assertEquals(3, keywords.size());
        assertEquals("word1", keywords.get(0));
        assertEquals("word2", keywords.get(1));
        assertEquals("word3", keywords.get(2));
    }

    @Test
    void testWordsWrapper_GetKeywordsList_WithSpaces() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords(" word1 , word2 , word3 ");
        List<String> keywords = wrapper.getKeywordsList();
        assertEquals(3, keywords.size());
        assertEquals("word1", keywords.get(0));
        assertEquals("word2", keywords.get(1));
        assertEquals("word3", keywords.get(2));
    }

    @Test
    void testWordsWrapper_GetKeywordsList_Empty() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords("");
        List<String> keywords = wrapper.getKeywordsList();
        assertNotNull(keywords);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testWordsWrapper_GetKeywordsList_Null() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords(null);
        List<String> keywords = wrapper.getKeywordsList();
        assertNotNull(keywords);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testWordsWrapper_GetKeywordsList_Blank() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords("   ");
        List<String> keywords = wrapper.getKeywordsList();
        assertNotNull(keywords);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testWordsWrapper_GetKeywordsList_TrailingComma() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords("word1,word2,");
        List<String> keywords = wrapper.getKeywordsList();
        assertEquals(2, keywords.size());
    }

    @Test
    void testWordsWrapper_GetKeywordsList_SingleWord() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords("single");
        List<String> keywords = wrapper.getKeywordsList();
        assertEquals(1, keywords.size());
        assertEquals("single", keywords.get(0));
    }

    @Test
    void testWordsWrapper_Fields() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        wrapper.setKeywords("k1");
        wrapper.setReplace("replacement");
        wrapper.setInputEnable(true);
        wrapper.setInputReplyText("input reply");
        wrapper.setOutputEnable(true);
        wrapper.setOutputReplyText("output reply");

        assertEquals("k1", wrapper.getKeywords());
        assertEquals("replacement", wrapper.getReplace());
        assertTrue(wrapper.isInputEnable());
        assertEquals("input reply", wrapper.getInputReplyText());
        assertTrue(wrapper.isOutputEnable());
        assertEquals("output reply", wrapper.getOutputReplyText());
    }

    @Test
    void testWordsWrapper_DefaultValues() {
        SensitiveEntity.WordsWrapper wrapper = new SensitiveEntity.WordsWrapper();
        assertNull(wrapper.getKeywords());
        assertNull(wrapper.getReplace());
        assertFalse(wrapper.isInputEnable());
        assertFalse(wrapper.isOutputEnable());
    }
}
