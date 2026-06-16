/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class AgentExecutionInfoTest {

    @Test
    void testAllGettersSetters() {
        List<AgentInvokeInfo> invokeList = List.of(new AgentInvokeInfo().setNodeId("n1"));
        Object metaData = "metaData";
        AgentExecutionInfo obj = new AgentExecutionInfo()
                .setConversationId("conv-001")
                .setExecutionId("exec-001")
                .setInputs("{\"key\":\"value\"}")
                .setOutputs("{\"result\":\"data\"}")
                .setErrorInfo("error")
                .setInvokeList(invokeList)
                .setStatus("running")
                .setStartTime(1000L)
                .setEndTime(2000L)
                .setMetaData(metaData);
        assertEquals("conv-001", obj.getConversationId());
        assertEquals("exec-001", obj.getExecutionId());
        assertEquals("{\"key\":\"value\"}", obj.getInputs());
        assertEquals("{\"result\":\"data\"}", obj.getOutputs());
        assertEquals("error", obj.getErrorInfo());
        assertEquals(invokeList, obj.getInvokeList());
        assertEquals("running", obj.getStatus());
        assertEquals(1000L, obj.getStartTime());
        assertEquals(2000L, obj.getEndTime());
        assertEquals(metaData, obj.getMetaData());
    }

    @Test
    void testEquals_SameObject() {
        AgentExecutionInfo obj = new AgentExecutionInfo().setConversationId("conv-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        AgentExecutionInfo obj1 = new AgentExecutionInfo()
                .setConversationId("conv-001").setExecutionId("exec-001")
                .setInputs("in").setOutputs("out")
                .setErrorInfo("err").setStatus("running")
                .setStartTime(1000L).setEndTime(2000L).setMetaData("meta");
        AgentExecutionInfo obj2 = new AgentExecutionInfo()
                .setConversationId("conv-001").setExecutionId("exec-001")
                .setInputs("in").setOutputs("out")
                .setErrorInfo("err").setStatus("running")
                .setStartTime(1000L).setEndTime(2000L).setMetaData("meta");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        AgentExecutionInfo obj = new AgentExecutionInfo().setConversationId("conv-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        AgentExecutionInfo obj = new AgentExecutionInfo().setConversationId("conv-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        AgentExecutionInfo obj1 = new AgentExecutionInfo().setConversationId("conv-001");
        AgentExecutionInfo obj2 = new AgentExecutionInfo().setConversationId("conv-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        AgentExecutionInfo obj1 = new AgentExecutionInfo()
                .setConversationId("conv-001").setExecutionId("exec-001");
        AgentExecutionInfo obj2 = new AgentExecutionInfo()
                .setConversationId("conv-001").setExecutionId("exec-001");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        AgentExecutionInfo obj = new AgentExecutionInfo().setConversationId("conv-001");
        assertNotNull(obj.toString());
    }
}
