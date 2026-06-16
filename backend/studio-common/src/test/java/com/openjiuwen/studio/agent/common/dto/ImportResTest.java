/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class ImportResTest {

    @Test
    void testAllGettersSetters() {
        ImportRes dep = new ImportRes().setId("dep-001").setName("dependency");
        List<ImportRes> dependencies = List.of(dep);
        ImportRes obj = new ImportRes()
                .setId("id-001")
                .setName("import1")
                .setType("workflow")
                .setVersion("1.0")
                .setStatus("success")
                .setDetail("detail info")
                .setSuggestion("no issues")
                .setConfigDisplay(true)
                .setDependencies(dependencies);
        assertEquals("id-001", obj.getId());
        assertEquals("import1", obj.getName());
        assertEquals("workflow", obj.getType());
        assertEquals("1.0", obj.getVersion());
        assertEquals("success", obj.getStatus());
        assertEquals("detail info", obj.getDetail());
        assertEquals("no issues", obj.getSuggestion());
        assertTrue(obj.isConfigDisplay());
        assertEquals(dependencies, obj.getDependencies());
    }

    @Test
    void testEquals_SameObject() {
        ImportRes obj = new ImportRes().setId("id-001");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        ImportRes obj1 = new ImportRes()
                .setId("id-001").setName("import1").setType("workflow")
                .setVersion("1.0").setStatus("success").setDetail("detail")
                .setSuggestion("sug").setConfigDisplay(true);
        ImportRes obj2 = new ImportRes()
                .setId("id-001").setName("import1").setType("workflow")
                .setVersion("1.0").setStatus("success").setDetail("detail")
                .setSuggestion("sug").setConfigDisplay(true);
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        ImportRes obj = new ImportRes().setId("id-001");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        ImportRes obj = new ImportRes().setId("id-001");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        ImportRes obj1 = new ImportRes().setId("id-001");
        ImportRes obj2 = new ImportRes().setId("id-002");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        ImportRes obj1 = new ImportRes().setId("id-001").setName("import1");
        ImportRes obj2 = new ImportRes().setId("id-001").setName("import1");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        ImportRes obj = new ImportRes().setId("id-001");
        assertNotNull(obj.toString());
    }
}
