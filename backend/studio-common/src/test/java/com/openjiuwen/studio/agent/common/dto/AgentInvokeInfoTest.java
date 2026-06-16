/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AgentInvokeInfoTest {

    @Test
    void testAllGettersSetters() {
        Object inputs = "inputData";
        Object outputs = "outputData";
        Object metaData = "metaData";
        AgentInvokeInfo obj = new AgentInvokeInfo()
                .setNodeId("node-001")
                .setNodeName("LLM Node")
                .setNodeType("llm")
                .setNodeStatus("running")
                .setModelDeploymentId("deploy-001")
                .setErrorMessage("error")
                .setInputs(inputs)
                .setOutputs(outputs)
                .setStartTime(1000L)
                .setEndTime(2000L)
                .setMetaData(metaData);
        assertEquals("node-001", obj.getNodeId());
        assertEquals("LLM Node", obj.getNodeName());
        assertEquals("llm", obj.getNodeType());
        assertEquals("running", obj.getNodeStatus());
        assertEquals("deploy-001", obj.getModelDeploymentId());
        assertEquals("error", obj.getErrorMessage());
        assertEquals(inputs, obj.getInputs());
        assertEquals(outputs, obj.getOutputs());
        assertEquals(1000L, obj.getStartTime());
        assertEquals(2000L, obj.getEndTime());
        assertEquals(metaData, obj.getMetaData());
    }

    @Test
    void testEquals_SameObject() {
        AgentInvokeInfo obj = new AgentInvokeInfo().setNodeId("node-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        AgentInvokeInfo obj1 = new AgentInvokeInfo()
                .setNodeId("node-001").setNodeName("LLM")
                .setNodeType("llm").setNodeStatus("running")
                .setModelDeploymentId("deploy-001").setErrorMessage("err")
                .setInputs("in").setOutputs("out")
                .setStartTime(1000L).setEndTime(2000L).setMetaData("meta");
        AgentInvokeInfo obj2 = new AgentInvokeInfo()
                .setNodeId("node-001").setNodeName("LLM")
                .setNodeType("llm").setNodeStatus("running")
                .setModelDeploymentId("deploy-001").setErrorMessage("err")
                .setInputs("in").setOutputs("out")
                .setStartTime(1000L).setEndTime(2000L).setMetaData("meta");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        AgentInvokeInfo obj = new AgentInvokeInfo().setNodeId("node-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        AgentInvokeInfo obj = new AgentInvokeInfo().setNodeId("node-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        AgentInvokeInfo obj1 = new AgentInvokeInfo().setNodeId("node-001");
        AgentInvokeInfo obj2 = new AgentInvokeInfo().setNodeId("node-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        AgentInvokeInfo obj1 = new AgentInvokeInfo().setNodeId("node-001").setNodeName("LLM");
        AgentInvokeInfo obj2 = new AgentInvokeInfo().setNodeId("node-001").setNodeName("LLM");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AgentInvokeInfo obj = new AgentInvokeInfo().setNodeId("node-001");
        assertNotNull(obj.toString());
    }
}
