/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConversationInfoTest {

    @Test
    void testAllGettersSetters() {
        Object inputs = "inputData";
        Object outputs = "outputData";
        ConversationInfo obj = new ConversationInfo()
                .setProjectId("proj-001")
                .setWorkflowId("wf-001")
                .setConversationId("conv-001")
                .setInputs(inputs)
                .setOutputs(outputs)
                .setStatus("running")
                .setStartTime(1000L)
                .setEndTime(2000L)
                .setSuccessCount(5)
                .setFailureCount(2);
        assertEquals("proj-001", obj.getProjectId());
        assertEquals("wf-001", obj.getWorkflowId());
        assertEquals("conv-001", obj.getConversationId());
        assertEquals(inputs, obj.getInputs());
        assertEquals(outputs, obj.getOutputs());
        assertEquals("running", obj.getStatus());
        assertEquals(1000L, obj.getStartTime());
        assertEquals(2000L, obj.getEndTime());
        assertEquals(5, obj.getSuccessCount());
        assertEquals(2, obj.getFailureCount());
    }

    @Test
    void testEquals_SameObject() {
        ConversationInfo obj = new ConversationInfo().setProjectId("proj-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        ConversationInfo obj1 = new ConversationInfo()
                .setProjectId("proj-001").setWorkflowId("wf-001")
                .setConversationId("conv-001").setInputs("in").setOutputs("out")
                .setStatus("running").setStartTime(1000L).setEndTime(2000L)
                .setSuccessCount(5).setFailureCount(2);
        ConversationInfo obj2 = new ConversationInfo()
                .setProjectId("proj-001").setWorkflowId("wf-001")
                .setConversationId("conv-001").setInputs("in").setOutputs("out")
                .setStatus("running").setStartTime(1000L).setEndTime(2000L)
                .setSuccessCount(5).setFailureCount(2);
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        ConversationInfo obj = new ConversationInfo().setProjectId("proj-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        ConversationInfo obj = new ConversationInfo().setProjectId("proj-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        ConversationInfo obj1 = new ConversationInfo().setProjectId("proj-001");
        ConversationInfo obj2 = new ConversationInfo().setProjectId("proj-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        ConversationInfo obj1 = new ConversationInfo()
                .setProjectId("proj-001").setWorkflowId("wf-001");
        ConversationInfo obj2 = new ConversationInfo()
                .setProjectId("proj-001").setWorkflowId("wf-001");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ConversationInfo obj = new ConversationInfo().setProjectId("proj-001");
        assertNotNull(obj.toString());
    }
}
