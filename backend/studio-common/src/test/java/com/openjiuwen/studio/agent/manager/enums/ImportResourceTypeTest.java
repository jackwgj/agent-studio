/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportResourceTypeTest {

    @Test
    void testEnumValues() {
        ImportResourceType[] values = ImportResourceType.values();
        assertEquals(3, values.length);
    }

    @Test
    void testAgent() {
        assertEquals("AGENT", ImportResourceType.AGENT.toString());
    }

    @Test
    void testWorkflow() {
        assertEquals("WORKFLOW", ImportResourceType.WORKFLOW.toString());
    }

    @Test
    void testPlugin() {
        assertEquals("PLUGIN", ImportResourceType.PLUGIN.toString());
    }

    @Test
    void testValueOf() {
        assertEquals(ImportResourceType.AGENT, ImportResourceType.valueOf("AGENT"));
        assertEquals(ImportResourceType.WORKFLOW, ImportResourceType.valueOf("WORKFLOW"));
        assertEquals(ImportResourceType.PLUGIN, ImportResourceType.valueOf("PLUGIN"));
    }
}
