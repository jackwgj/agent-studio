/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AdditionalQuestionsReqTest {

    @Test
    void testAllSettersAndGetters_FluentChain() {
        AdditionalQuestionsReq req = new AdditionalQuestionsReq()
                .setName("question1")
                .setDescription("desc1")
                .setPrompt("prompt1")
                .setEnable(true)
                .setVersionId("ver-001");
        assertEquals("question1", req.getName());
        assertEquals("desc1", req.getDescription());
        assertEquals("prompt1", req.getPrompt());
        assertTrue(req.isEnable());
        assertEquals("ver-001", req.getVersionId());
    }

    @Test
    void testEquals_SameInstance() {
        AdditionalQuestionsReq req = new AdditionalQuestionsReq().setName("q");
        assertEquals(req, req);
    }

    @Test
    void testEquals_EqualObjects() {
        AdditionalQuestionsReq r1 = new AdditionalQuestionsReq().setName("q").setEnable(true);
        AdditionalQuestionsReq r2 = new AdditionalQuestionsReq().setName("q").setEnable(true);
        assertEquals(r1, r2);
    }

    @Test
    void testEquals_Null() {
        AdditionalQuestionsReq req = new AdditionalQuestionsReq();
        assertNotEquals(null, req);
    }

    @Test
    void testEquals_DifferentClass() {
        AdditionalQuestionsReq req = new AdditionalQuestionsReq();
        assertNotEquals("string", req);
    }

    @Test
    void testHashCode_Consistency() {
        AdditionalQuestionsReq r1 = new AdditionalQuestionsReq().setName("q").setEnable(true);
        AdditionalQuestionsReq r2 = new AdditionalQuestionsReq().setName("q").setEnable(true);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AdditionalQuestionsReq req = new AdditionalQuestionsReq().setName("q");
        assertNotNull(req.toString());
    }
}
