/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.prompt.engineering.dto.VariableVo;

import org.junit.jupiter.api.Test;

/**
 * PeVariable 实体转换器单元测试
 */
class PeVariableConverterTest {

    @Test
    void testConvertToVariableVo_FullFields() {
        PeVariable variable = new PeVariable();
        variable.setId("var-001");
        variable.setName("变量1");
        variable.setKey("key1");
        variable.setPeTaskId("task-001");

        VariableVo vo = variable.convertToVariableVo();

        assertEquals("var-001", vo.getId());
        assertEquals("变量1", vo.getName());
        assertEquals("key1", vo.getKey());
        assertEquals("task-001", vo.getPeTaskId());
    }

    @Test
    void testConvertToVariableVo_NullFields() {
        PeVariable variable = new PeVariable();
        variable.setId(null);
        variable.setName(null);
        variable.setKey(null);
        variable.setPeTaskId(null);

        VariableVo vo = variable.convertToVariableVo();

        assertNull(vo.getId());
        assertNull(vo.getName());
        assertNull(vo.getKey());
        assertNull(vo.getPeTaskId());
    }

    @Test
    void testConvertToVariableVo_PartialFields() {
        PeVariable variable = new PeVariable();
        variable.setId("var-002");
        variable.setKey("key2");

        VariableVo vo = variable.convertToVariableVo();

        assertEquals("var-002", vo.getId());
        assertNull(vo.getName());
        assertEquals("key2", vo.getKey());
        assertNull(vo.getPeTaskId());
    }
}
