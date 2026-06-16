/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AdditionalQuestionsWorkflowReqTest {

    @Test
    void testAllGettersSetters() {
        AdditionalQuestionsWorkflowReq obj = new AdditionalQuestionsWorkflowReq()
                .setName("questions1")
                .setDescription("auto questions")
                .setEnable(true)
                .setPrompt("generate questions")
                .setVersionId("ver-001")
                .setModelId("model-001")
                .setModelName("GPT-4")
                .setModelType("llm");
        assertEquals("questions1", obj.getName());
        assertEquals("auto questions", obj.getDescription());
        assertTrue(obj.isEnable());
        assertEquals("generate questions", obj.getPrompt());
        assertEquals("ver-001", obj.getVersionId());
        assertEquals("model-001", obj.getModelId());
        assertEquals("GPT-4", obj.getModelName());
        assertEquals("llm", obj.getModelType());
    }

    @Test
    void testEquals_SameObject() {
        AdditionalQuestionsWorkflowReq obj = new AdditionalQuestionsWorkflowReq().setName("q1");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        AdditionalQuestionsWorkflowReq obj1 = new AdditionalQuestionsWorkflowReq()
                .setName("q1").setDescription("desc").setEnable(true)
                .setPrompt("prompt").setVersionId("v1")
                .setModelId("m1").setModelName("mn").setModelType("mt");
        AdditionalQuestionsWorkflowReq obj2 = new AdditionalQuestionsWorkflowReq()
                .setName("q1").setDescription("desc").setEnable(true)
                .setPrompt("prompt").setVersionId("v1")
                .setModelId("m1").setModelName("mn").setModelType("mt");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        AdditionalQuestionsWorkflowReq obj = new AdditionalQuestionsWorkflowReq().setName("q1");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        AdditionalQuestionsWorkflowReq obj = new AdditionalQuestionsWorkflowReq().setName("q1");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        AdditionalQuestionsWorkflowReq obj1 = new AdditionalQuestionsWorkflowReq().setName("q1");
        AdditionalQuestionsWorkflowReq obj2 = new AdditionalQuestionsWorkflowReq().setName("q2");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        AdditionalQuestionsWorkflowReq obj1 = new AdditionalQuestionsWorkflowReq()
                .setName("q1").setDescription("desc");
        AdditionalQuestionsWorkflowReq obj2 = new AdditionalQuestionsWorkflowReq()
                .setName("q1").setDescription("desc");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AdditionalQuestionsWorkflowReq obj = new AdditionalQuestionsWorkflowReq().setName("q1");
        assertNotNull(obj.toString());
    }
}
