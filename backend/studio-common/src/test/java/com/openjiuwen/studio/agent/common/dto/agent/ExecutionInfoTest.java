/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class ExecutionInfoTest {

    @Test
    void testAllGettersSetters() {
        Object inputs = "inputData";
        Object outputs = "outputData";
        List<NodeRunInfo> eventList = List.of(new NodeRunInfo().setAgentId("a1"));
        ExecutionInfo obj = new ExecutionInfo()
                .setProjectId("proj-001")
                .setWorkflowId("wf-001")
                .setConversationId("conv-001")
                .setExecutionId("exec-001")
                .setInputs(inputs)
                .setOutputs(outputs)
                .setStatus("running")
                .setRunInfo("info")
                .setStartTime(1000L)
                .setEndTime(2000L)
                .setErrorInfo("error")
                .setEventList(eventList);
        assertEquals("proj-001", obj.getProjectId());
        assertEquals("wf-001", obj.getWorkflowId());
        assertEquals("conv-001", obj.getConversationId());
        assertEquals("exec-001", obj.getExecutionId());
        assertEquals(inputs, obj.getInputs());
        assertEquals(outputs, obj.getOutputs());
        assertEquals("running", obj.getStatus());
        assertEquals("info", obj.getRunInfo());
        assertEquals(1000L, obj.getStartTime());
        assertEquals(2000L, obj.getEndTime());
        assertEquals("error", obj.getErrorInfo());
        assertEquals(eventList, obj.getEventList());
    }

    @Test
    void testEquals_SameObject() {
        ExecutionInfo obj = new ExecutionInfo().setProjectId("proj-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        ExecutionInfo obj1 = new ExecutionInfo()
                .setProjectId("proj-001").setWorkflowId("wf-001")
                .setConversationId("conv-001").setExecutionId("exec-001")
                .setInputs("in").setOutputs("out")
                .setStatus("running").setRunInfo("info")
                .setStartTime(1000L).setEndTime(2000L)
                .setErrorInfo("err");
        ExecutionInfo obj2 = new ExecutionInfo()
                .setProjectId("proj-001").setWorkflowId("wf-001")
                .setConversationId("conv-001").setExecutionId("exec-001")
                .setInputs("in").setOutputs("out")
                .setStatus("running").setRunInfo("info")
                .setStartTime(1000L).setEndTime(2000L)
                .setErrorInfo("err");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        ExecutionInfo obj = new ExecutionInfo().setProjectId("proj-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        ExecutionInfo obj = new ExecutionInfo().setProjectId("proj-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        ExecutionInfo obj1 = new ExecutionInfo().setProjectId("proj-001");
        ExecutionInfo obj2 = new ExecutionInfo().setProjectId("proj-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        ExecutionInfo obj1 = new ExecutionInfo().setProjectId("proj-001").setWorkflowId("wf-001");
        ExecutionInfo obj2 = new ExecutionInfo().setProjectId("proj-001").setWorkflowId("wf-001");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ExecutionInfo obj = new ExecutionInfo().setProjectId("proj-001");
        assertNotNull(obj.toString());
    }
}
