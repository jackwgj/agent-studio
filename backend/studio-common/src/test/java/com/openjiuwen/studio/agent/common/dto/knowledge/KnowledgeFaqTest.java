/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KnowledgeFaqTest {

    @Test
    void testAllGettersSetters() {
        KnowledgeFaq obj = new KnowledgeFaq()
                .setFaqId("faq-001")
                .setKnowledgeRepoId("repo-001")
                .setQuestion("What is this?")
                .setAnswer("This is a test.")
                .setQuestion1("Q1")
                .setQuestion2("Q2")
                .setQuestion3("Q3")
                .setQuestion4("Q4")
                .setCreateTime(1000L)
                .setUpdateTime(2000L)
                .setCategory("general")
                .setTags("tag1,tag2");
        assertEquals("faq-001", obj.getFaqId());
        assertEquals("repo-001", obj.getKnowledgeRepoId());
        assertEquals("What is this?", obj.getQuestion());
        assertEquals("This is a test.", obj.getAnswer());
        assertEquals("Q1", obj.getQuestion1());
        assertEquals("Q2", obj.getQuestion2());
        assertEquals("Q3", obj.getQuestion3());
        assertEquals("Q4", obj.getQuestion4());
        assertEquals(1000L, obj.getCreateTime());
        assertEquals(2000L, obj.getUpdateTime());
        assertEquals("general", obj.getCategory());
        assertEquals("tag1,tag2", obj.getTags());
    }

    @Test
    void testEquals_SameObject() {
        KnowledgeFaq obj = new KnowledgeFaq().setFaqId("faq-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        KnowledgeFaq obj1 = new KnowledgeFaq()
                .setFaqId("faq-001").setKnowledgeRepoId("repo-001")
                .setQuestion("Q").setAnswer("A")
                .setQuestion1("Q1").setQuestion2("Q2")
                .setQuestion3("Q3").setQuestion4("Q4")
                .setCreateTime(1000L).setUpdateTime(2000L)
                .setCategory("cat").setTags("tags");
        KnowledgeFaq obj2 = new KnowledgeFaq()
                .setFaqId("faq-001").setKnowledgeRepoId("repo-001")
                .setQuestion("Q").setAnswer("A")
                .setQuestion1("Q1").setQuestion2("Q2")
                .setQuestion3("Q3").setQuestion4("Q4")
                .setCreateTime(1000L).setUpdateTime(2000L)
                .setCategory("cat").setTags("tags");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        KnowledgeFaq obj = new KnowledgeFaq().setFaqId("faq-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        KnowledgeFaq obj = new KnowledgeFaq().setFaqId("faq-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        KnowledgeFaq obj1 = new KnowledgeFaq().setFaqId("faq-001");
        KnowledgeFaq obj2 = new KnowledgeFaq().setFaqId("faq-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        KnowledgeFaq obj1 = new KnowledgeFaq().setFaqId("faq-001").setKnowledgeRepoId("repo-001");
        KnowledgeFaq obj2 = new KnowledgeFaq().setFaqId("faq-001").setKnowledgeRepoId("repo-001");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        KnowledgeFaq obj = new KnowledgeFaq().setFaqId("faq-001");
        assertNotNull(obj.toString());
    }
}
