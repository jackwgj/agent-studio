/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FaqInfoTest {

    @Test
    void testGettersAndSetters_AllFields() {
        FaqInfo info = new FaqInfo();
        info.setId("id-1");
        info.setRepoId("repo-1");
        info.setQuestion("What?");
        info.setAnswer("This.");
        info.setQuestion1("Q1");
        info.setQuestion2("Q2");
        info.setQuestion3("Q3");
        info.setQuestion4("Q4");
        info.setCategory("general");
        info.setTags("tag1,tag2");
        info.setCreateTime("1000");
        info.setUpdateTime("2000");

        assertEquals("id-1", info.getId());
        assertEquals("repo-1", info.getRepoId());
        assertEquals("What?", info.getQuestion());
        assertEquals("This.", info.getAnswer());
        assertEquals("Q1", info.getQuestion1());
        assertEquals("Q2", info.getQuestion2());
        assertEquals("Q3", info.getQuestion3());
        assertEquals("Q4", info.getQuestion4());
        assertEquals("general", info.getCategory());
        assertEquals("tag1,tag2", info.getTags());
        assertEquals("1000", info.getCreateTime());
        assertEquals("2000", info.getUpdateTime());
    }

    @Test
    void testBuilder() {
        FaqInfo info = FaqInfo.builder()
                .id("id-1")
                .repoId("repo-1")
                .question("What?")
                .answer("This.")
                .question1("Q1")
                .question2("Q2")
                .question3("Q3")
                .question4("Q4")
                .category("general")
                .tags("tag1")
                .createTime("1000")
                .updateTime("2000")
                .build();

        assertEquals("id-1", info.getId());
        assertEquals("repo-1", info.getRepoId());
        assertEquals("What?", info.getQuestion());
        assertEquals("This.", info.getAnswer());
    }

    @Test
    void testAllArgsConstructor() {
        FaqInfo info = new FaqInfo("id-1", "repo-1", "q", "a", "q1", "q2", "q3", "q4", "cat", "tags", "100", "200");
        assertEquals("id-1", info.getId());
        assertEquals("repo-1", info.getRepoId());
        assertEquals("100", info.getCreateTime());
        assertEquals("200", info.getUpdateTime());
    }

    @Test
    void testNoArgsConstructor() {
        FaqInfo info = new FaqInfo();
        assertNull(info.getId());
        assertNull(info.getRepoId());
    }

    @Test
    void testEquals_SameObject() {
        FaqInfo info = new FaqInfo();
        info.setId("id");
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObject() {
        FaqInfo i1 = new FaqInfo();
        i1.setId("id");
        i1.setQuestion("q");
        FaqInfo i2 = new FaqInfo();
        i2.setId("id");
        i2.setQuestion("q");
        assertEquals(i1, i2);
    }

    @Test
    void testEquals_Null() {
        FaqInfo info = new FaqInfo();
        assertNotEquals(null, info);
    }

    @Test
    void testEquals_DifferentClass() {
        FaqInfo info = new FaqInfo();
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        FaqInfo i1 = new FaqInfo();
        i1.setId("id");
        FaqInfo i2 = new FaqInfo();
        i2.setId("id");
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        FaqInfo info = new FaqInfo();
        info.setId("id");
        assertNotNull(info.toString());
    }

    @Test
    void testConvertToDto_WithValidTimes() {
        FaqInfo info = FaqInfo.builder()
                .id("id-1")
                .repoId("repo-1")
                .question("q")
                .answer("a")
                .createTime("1000")
                .updateTime("2000")
                .build();

        KnowledgeFaq faq = info.convertToDto();
        assertNotNull(faq);
        assertEquals("id-1", faq.getFaqId());
        assertEquals(1000L, faq.getCreateTime());
        assertEquals(2000L, faq.getUpdateTime());
    }

    @Test
    void testConvertToDto_WithBlankTimes() {
        FaqInfo info = FaqInfo.builder()
                .id("id-1")
                .repoId("repo-1")
                .question("q")
                .answer("a")
                .createTime("")
                .updateTime("")
                .build();

        KnowledgeFaq faq = info.convertToDto();
        assertNotNull(faq);
    }
}
