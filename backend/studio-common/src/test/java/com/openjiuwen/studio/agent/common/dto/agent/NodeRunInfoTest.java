/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NodeRunInfoTest {

    @Test
    void testSetGetAgentId() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getAgentId());
        NodeRunInfo result = info.setAgentId("agent-001");
        assertSame(info, result);
        assertEquals("agent-001", info.getAgentId());
    }

    @Test
    void testSetGetNodeId() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getNodeId());
        NodeRunInfo result = info.setNodeId("node-001");
        assertSame(info, result);
        assertEquals("node-001", info.getNodeId());
    }

    @Test
    void testSetGetParentNodeId() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getParentNodeId());
        NodeRunInfo result = info.setParentNodeId("parent-001");
        assertSame(info, result);
        assertEquals("parent-001", info.getParentNodeId());
    }

    @Test
    void testSetGetNodeStatus() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getNodeStatus());
        NodeRunInfo result = info.setNodeStatus(NodeRunInfo.NodeStatusEnum.STARTED);
        assertSame(info, result);
        assertEquals(NodeRunInfo.NodeStatusEnum.STARTED, info.getNodeStatus());
    }

    @Test
    void testSetGetParentWorkflowId() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getParentWorkflowId());
        NodeRunInfo result = info.setParentWorkflowId("wf-001");
        assertSame(info, result);
        assertEquals("wf-001", info.getParentWorkflowId());
    }

    @Test
    void testSetGetLoopIndex() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getLoopIndex());
        NodeRunInfo result = info.setLoopIndex(3);
        assertSame(info, result);
        assertEquals(3, info.getLoopIndex());
    }

    @Test
    void testSetGetLoopNodeId() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getLoopNodeId());
        NodeRunInfo result = info.setLoopNodeId("loop-node-001");
        assertSame(info, result);
        assertEquals("loop-node-001", info.getLoopNodeId());
    }

    @Test
    void testSetGetStatus() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getStatus());
        Status status = new Status().setCode(200).setDesc("OK");
        NodeRunInfo result = info.setStatus(status);
        assertSame(info, result);
        assertEquals(status, info.getStatus());
    }

    @Test
    void testSetGetNodeName() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getNodeName());
        NodeRunInfo result = info.setNodeName("LLM Node");
        assertSame(info, result);
        assertEquals("LLM Node", info.getNodeName());
    }

    @Test
    void testSetGetNodeType() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getNodeType());
        NodeRunInfo result = info.setNodeType("llm");
        assertSame(info, result);
        assertEquals("llm", info.getNodeType());
    }

    @Test
    void testSetGetErrorMessage() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getErrorMessage());
        NodeRunInfo result = info.setErrorMessage("error occurred");
        assertSame(info, result);
        assertEquals("error occurred", info.getErrorMessage());
    }

    @Test
    void testSetGetInputs() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getInputs());
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("key", "value");
        NodeRunInfo result = info.setInputs(inputs);
        assertSame(info, result);
        assertEquals(inputs, info.getInputs());
    }

    @Test
    void testSetGetOutputs() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getOutputs());
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("result", "data");
        NodeRunInfo result = info.setOutputs(outputs);
        assertSame(info, result);
        assertEquals(outputs, info.getOutputs());
    }

    @Test
    void testSetGetMessages() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getMessages());
        List<Message> messages = List.of(new Message().setRole("user").setContent("hello"));
        NodeRunInfo result = info.setMessages(messages);
        assertSame(info, result);
        assertEquals(messages, info.getMessages());
    }

    @Test
    void testSetGetMetadata() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getMetadata());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("meta", "data");
        NodeRunInfo result = info.setMetadata(metadata);
        assertSame(info, result);
        assertEquals(metadata, info.getMetadata());
    }

    @Test
    void testSetGetStartTime() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getStartTime());
        NodeRunInfo result = info.setStartTime(1000L);
        assertSame(info, result);
        assertEquals(1000L, info.getStartTime());
    }

    @Test
    void testSetGetEndTime() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getEndTime());
        NodeRunInfo result = info.setEndTime(2000L);
        assertSame(info, result);
        assertEquals(2000L, info.getEndTime());
    }

    @Test
    void testSetGetStartupTime() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getStartupTime());
        NodeRunInfo result = info.setStartupTime(500L);
        assertSame(info, result);
        assertEquals(500L, info.getStartupTime());
    }

    @Test
    void testSetGetPrefillTime() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getPrefillTime());
        NodeRunInfo result = info.setPrefillTime(300L);
        assertSame(info, result);
        assertEquals(300L, info.getPrefillTime());
    }

    @Test
    void testSetGetExecutionId() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getExecutionId());
        NodeRunInfo result = info.setExecutionId("exec-001");
        assertSame(info, result);
        assertEquals("exec-001", info.getExecutionId());
    }

    @Test
    void testSetGetInnerError() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getInnerError());
        NodeRunInfoInnerError innerError = new NodeRunInfoInnerError();
        NodeRunInfo result = info.setInnerError(innerError);
        assertSame(info, result);
        assertEquals(innerError, info.getInnerError());
    }

    @Test
    void testSetGetMemory() {
        NodeRunInfo info = new NodeRunInfo();
        assertNull(info.getMemory());
        Map<String, Object> memory = new HashMap<>();
        memory.put("history", "data");
        NodeRunInfo result = info.setMemory(memory);
        assertSame(info, result);
        assertEquals(memory, info.getMemory());
    }

    @Test
    void testChainedSetters() {
        NodeRunInfo info = new NodeRunInfo()
                .setAgentId("agent-001")
                .setNodeId("node-001")
                .setNodeName("Test Node")
                .setNodeType("llm")
                .setStartTime(1000L)
                .setEndTime(2000L)
                .setExecutionId("exec-001");
        assertEquals("agent-001", info.getAgentId());
        assertEquals("node-001", info.getNodeId());
        assertEquals("Test Node", info.getNodeName());
        assertEquals("llm", info.getNodeType());
        assertEquals(1000L, info.getStartTime());
        assertEquals(2000L, info.getEndTime());
        assertEquals("exec-001", info.getExecutionId());
    }

    @Test
    void testEquals_SameInstance() {
        NodeRunInfo info = new NodeRunInfo().setAgentId("agent-001");
        assertEquals(info, info);
    }

    @Test
    void testEquals_EqualObjects() {
        NodeRunInfo info1 = new NodeRunInfo().setAgentId("agent-001").setNodeId("node-001");
        NodeRunInfo info2 = new NodeRunInfo().setAgentId("agent-001").setNodeId("node-001");
        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void testEquals_DifferentObjects() {
        NodeRunInfo info1 = new NodeRunInfo().setAgentId("agent-001");
        NodeRunInfo info2 = new NodeRunInfo().setAgentId("agent-002");
        assertNotEquals(info1, info2);
    }

    @Test
    void testEquals_NullAndDifferentType() {
        NodeRunInfo info = new NodeRunInfo().setAgentId("agent-001");
        assertNotEquals(null, info);
        assertNotEquals("string", info);
    }

    @Test
    void testHashCode_Consistency() {
        NodeRunInfo info1 = new NodeRunInfo().setAgentId("agent-001").setNodeId("node-001");
        NodeRunInfo info2 = new NodeRunInfo().setAgentId("agent-001").setNodeId("node-001");
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjects() {
        NodeRunInfo info1 = new NodeRunInfo().setAgentId("agent-001");
        NodeRunInfo info2 = new NodeRunInfo().setAgentId("agent-002");
        assertNotEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void testToString() {
        NodeRunInfo info = new NodeRunInfo().setAgentId("agent-001").setNodeId("node-001");
        String str = info.toString();
        assertNotNull(str);
        assertTrue(str.contains("NodeRunInfo"));
        assertTrue(str.contains("agent-001"));
        assertTrue(str.contains("node-001"));
    }

    @Test
    void testToString_NullFields() {
        NodeRunInfo info = new NodeRunInfo();
        String str = info.toString();
        assertNotNull(str);
        assertTrue(str.contains("null"));
    }

    @Test
    void testNodeStatusEnum_Values() {
        NodeRunInfo.NodeStatusEnum[] values = NodeRunInfo.NodeStatusEnum.values();
        assertEquals(3, values.length);
    }

    @Test
    void testNodeStatusEnum_FromValue() {
        assertEquals(NodeRunInfo.NodeStatusEnum.STARTED, NodeRunInfo.NodeStatusEnum.fromValue("node_started"));
        assertEquals(NodeRunInfo.NodeStatusEnum.WAIT, NodeRunInfo.NodeStatusEnum.fromValue("node_wait"));
        assertEquals(NodeRunInfo.NodeStatusEnum.FINISHED, NodeRunInfo.NodeStatusEnum.fromValue("node_finished"));
    }

    @Test
    void testNodeStatusEnum_FromValue_Invalid() {
        assertNull(NodeRunInfo.NodeStatusEnum.fromValue("invalid"));
    }

    @Test
    void testNodeStatusEnum_ToString() {
        assertEquals("node_started", NodeRunInfo.NodeStatusEnum.STARTED.toString());
        assertEquals("node_wait", NodeRunInfo.NodeStatusEnum.WAIT.toString());
        assertEquals("node_finished", NodeRunInfo.NodeStatusEnum.FINISHED.toString());
    }

    @Test
    void testEquals_AllFieldsEqual() {
        Map<String, Object> inputs = Map.of("key", "value");
        Map<String, Object> outputs = Map.of("result", "data");
        Status status = new Status().setCode(200).setDesc("OK");

        NodeRunInfo info1 = new NodeRunInfo()
                .setAgentId("a1").setNodeId("n1").setParentNodeId("p1")
                .setNodeStatus(NodeRunInfo.NodeStatusEnum.STARTED)
                .setParentWorkflowId("pw1").setLoopIndex(1).setLoopNodeId("ln1")
                .setStatus(status).setNodeName("name").setNodeType("type")
                .setErrorMessage("err").setInputs(inputs).setOutputs(outputs)
                .setStartTime(1L).setEndTime(2L).setStartupTime(3L).setPrefillTime(4L)
                .setExecutionId("e1");

        NodeRunInfo info2 = new NodeRunInfo()
                .setAgentId("a1").setNodeId("n1").setParentNodeId("p1")
                .setNodeStatus(NodeRunInfo.NodeStatusEnum.STARTED)
                .setParentWorkflowId("pw1").setLoopIndex(1).setLoopNodeId("ln1")
                .setStatus(status).setNodeName("name").setNodeType("type")
                .setErrorMessage("err").setInputs(inputs).setOutputs(outputs)
                .setStartTime(1L).setEndTime(2L).setStartupTime(3L).setPrefillTime(4L)
                .setExecutionId("e1");

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
    }
}
