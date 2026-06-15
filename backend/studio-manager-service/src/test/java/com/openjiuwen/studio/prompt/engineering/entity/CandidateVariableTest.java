/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.alibaba.fastjson.JSON;

import org.junit.jupiter.api.Test;

/**
 * CandidateVariable 实体单元测试
 */
class CandidateVariableTest {

    @Test
    void testGetterSetter() {
        CandidateVariable cv = new CandidateVariable();
        cv.setId("var-001");
        cv.setValue("值1");

        assertEquals("var-001", cv.getId());
        assertEquals("值1", cv.getValue());
    }

    @Test
    void testJsonSerialization() {
        CandidateVariable cv = new CandidateVariable();
        cv.setId("var-002");
        cv.setValue("值2");

        String json = JSON.toJSONString(cv);
        assertEquals("{\"id\":\"var-002\",\"value\":\"值2\"}", json);
    }

    @Test
    void testJsonDeserialization() {
        String json = "{\"id\":\"var-003\",\"value\":\"值3\"}";
        CandidateVariable cv = JSON.parseObject(json, CandidateVariable.class);

        assertEquals("var-003", cv.getId());
        assertEquals("值3", cv.getValue());
    }

    @Test
    void testJsonArraySerialization() {
        CandidateVariable cv1 = new CandidateVariable();
        cv1.setId("var-001");
        cv1.setValue("值1");

        CandidateVariable cv2 = new CandidateVariable();
        cv2.setId("var-002");
        cv2.setValue("值2");

        String json = JSON.toJSONString(java.util.List.of(cv1, cv2));
        assertEquals("[{\"id\":\"var-001\",\"value\":\"值1\"},{\"id\":\"var-002\",\"value\":\"值2\"}]", json);
    }

    @Test
    void testJsonArrayDeserialization() {
        String json = "[{\"id\":\"var-001\",\"value\":\"值1\"},{\"id\":\"var-002\",\"value\":\"值2\"}]";
        java.util.List<CandidateVariable> list = JSON.parseArray(json, CandidateVariable.class);

        assertEquals(2, list.size());
        assertEquals("var-001", list.get(0).getId());
        assertEquals("值2", list.get(1).getValue());
    }

    @Test
    void testEquality() {
        CandidateVariable cv1 = new CandidateVariable();
        cv1.setId("var-001");
        cv1.setValue("值1");

        CandidateVariable cv2 = new CandidateVariable();
        cv2.setId("var-001");
        cv2.setValue("值1");

        // Lombok @Data generates equals/hashCode
        assertEquals(cv1, cv2);
        assertEquals(cv1.hashCode(), cv2.hashCode());
    }

    @Test
    void testInequality() {
        CandidateVariable cv1 = new CandidateVariable();
        cv1.setId("var-001");
        cv1.setValue("值1");

        CandidateVariable cv2 = new CandidateVariable();
        cv2.setId("var-002");
        cv2.setValue("值1");

        assertNotEquals(cv1, cv2);
    }

    @Test
    void testNullFields() {
        CandidateVariable cv = new CandidateVariable();
        assertEquals(null, cv.getId());
        assertEquals(null, cv.getValue());
    }
}
