/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for AgentIrType enum
 */
public class AgentIrTypeTest {

    @Test
    public void test_agent_ir_type_from_value() {
        // Test valid values
        assertEquals(AgentIrType.AGENT, AgentIrType.fromValue("agent"));
        assertEquals(AgentIrType.AGENT, AgentIrType.fromValue("AGENT"));
        assertEquals(AgentIrType.WORKFLOW, AgentIrType.fromValue("workflow"));
        assertEquals(AgentIrType.WORKFLOW, AgentIrType.fromValue("WORKFLOW"));
        assertEquals(AgentIrType.MULTI_AGENTS, AgentIrType.fromValue("multiagents"));
        assertEquals(AgentIrType.MULTI_AGENTS, AgentIrType.fromValue("MULTIAGENTS"));
        assertEquals(AgentIrType.DEEP_RESEARCH, AgentIrType.fromValue("deepresearch"));
        assertEquals(AgentIrType.DEEP_RESEARCH, AgentIrType.fromValue("DEEPRESEARCH"));
        assertEquals(AgentIrType.PLAN_EXECUTE, AgentIrType.fromValue("plan"));
        assertEquals(AgentIrType.PLAN_EXECUTE, AgentIrType.fromValue("PLAN"));

        // Test invalid value
        assertEquals(AgentIrType.UNKNOWN, AgentIrType.fromValue("invalid_type"));
        assertEquals(AgentIrType.UNKNOWN, AgentIrType.fromValue(null));
    }

    @Test
    public void test_agent_ir_type_to_string() {
        assertEquals("agent", AgentIrType.AGENT.toString());
        assertEquals("workflow", AgentIrType.WORKFLOW.toString());
        assertEquals("multiagents", AgentIrType.MULTI_AGENTS.toString());
        assertEquals("deepresearch", AgentIrType.DEEP_RESEARCH.toString());
        assertEquals("plan", AgentIrType.PLAN_EXECUTE.toString());
        assertEquals("unknown", AgentIrType.UNKNOWN.toString());
    }

    @Test
    public void test_agent_ir_type_values() {
        AgentIrType[] values = AgentIrType.values();
        assertEquals(6, values.length);
        assertTrue(Arrays.asList(values).contains(AgentIrType.AGENT));
        assertTrue(Arrays.asList(values).contains(AgentIrType.WORKFLOW));
        assertTrue(Arrays.asList(values).contains(AgentIrType.MULTI_AGENTS));
        assertTrue(Arrays.asList(values).contains(AgentIrType.DEEP_RESEARCH));
        assertTrue(Arrays.asList(values).contains(AgentIrType.PLAN_EXECUTE));
        assertTrue(Arrays.asList(values).contains(AgentIrType.UNKNOWN));
    }

    @Test
    public void test_agent_ir_type_get_type() {
        assertEquals("agent", AgentIrType.AGENT.getValue());
        assertEquals("workflow", AgentIrType.WORKFLOW.getValue());
        assertEquals("multiagents", AgentIrType.MULTI_AGENTS.getValue());
        assertEquals("deepresearch", AgentIrType.DEEP_RESEARCH.getValue());
        assertEquals("plan", AgentIrType.PLAN_EXECUTE.getValue());
        assertEquals("unknown", AgentIrType.UNKNOWN.getValue());
    }
}
