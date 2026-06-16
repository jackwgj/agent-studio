/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FaqReqTest {

    @Test
    void testGettersAndSetters_AllFields() {
        FaqReq req = new FaqReq();
        req.setQuestion("What?");
        req.setQuestion1("Q1");
        req.setQuestion2("Q2");
        req.setQuestion3("Q3");
        req.setQuestion4("Q4");
        req.setAnswer("This.");
        req.setRepoId("repo-1");
        req.setId("id-1");
        req.setCategory("general");
        req.setTags("tag1");

        assertEquals("What?", req.getQuestion());
        assertEquals("Q1", req.getQuestion1());
        assertEquals("Q2", req.getQuestion2());
        assertEquals("Q3", req.getQuestion3());
        assertEquals("Q4", req.getQuestion4());
        assertEquals("This.", req.getAnswer());
        assertEquals("repo-1", req.getRepoId());
        assertEquals("id-1", req.getId());
        assertEquals("general", req.getCategory());
        assertEquals("tag1", req.getTags());
    }

    @Test
    void testBuilder() {
        FaqReq req = FaqReq.builder()
                .question("q")
                .answer("a")
                .repoId("repo")
                .id("id")
                .build();

        assertEquals("q", req.getQuestion());
        assertEquals("a", req.getAnswer());
        assertEquals("repo", req.getRepoId());
        assertEquals("id", req.getId());
    }

    @Test
    void testNoArgsConstructor() {
        FaqReq req = new FaqReq();
        assertNull(req.getQuestion());
        assertNull(req.getId());
    }

    @Test
    void testEquals_SameObject() {
        FaqReq req = new FaqReq();
        req.setId("id");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObject() {
        FaqReq r1 = new FaqReq();
        r1.setId("id");
        r1.setQuestion("q");
        FaqReq r2 = new FaqReq();
        r2.setId("id");
        r2.setQuestion("q");
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        FaqReq req = new FaqReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        FaqReq req = new FaqReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        FaqReq r1 = new FaqReq();
        r1.setId("id");
        FaqReq r2 = new FaqReq();
        r2.setId("id");
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        FaqReq req = new FaqReq();
        req.setId("id");
        assertNotNull(req.toString());
    }
}
