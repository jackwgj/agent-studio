/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.prompt.engineering.dto.SmallPromptDto;

import org.junit.jupiter.api.Test;

/**
 * SmallPrompt 实体转换器单元测试
 */
class SmallPromptConverterTest {

    @Test
    void testConvertToSmallPrompt_FullFields() {
        SmallPromptDto dto = new SmallPromptDto();
        dto.setId("prompt-001");
        dto.setName("小型Prompt");
        dto.setManualScore(8);

        SmallPrompt prompt = new SmallPrompt().convertToSmallPrompt(dto);

        assertEquals("prompt-001", prompt.getId());
        assertEquals("小型Prompt", prompt.getName());
        assertEquals(8, prompt.getManualScore());
    }

    @Test
    void testConvertToSmallPrompt_PartialFields() {
        SmallPromptDto dto = new SmallPromptDto();
        dto.setId("prompt-002");
        dto.setName(null);
        dto.setManualScore(5);

        SmallPrompt prompt = new SmallPrompt().convertToSmallPrompt(dto);

        assertEquals("prompt-002", prompt.getId());
        assertNull(prompt.getName());
        assertEquals(5, prompt.getManualScore());
    }

    @Test
    void testConvertToSmallPrompt_NameOnly() {
        SmallPromptDto dto = new SmallPromptDto();
        dto.setId("prompt-003");
        dto.setName("仅名称");
        dto.setManualScore(null);

        SmallPrompt prompt = new SmallPrompt().convertToSmallPrompt(dto);

        assertEquals("prompt-003", prompt.getId());
        assertEquals("仅名称", prompt.getName());
        assertNull(prompt.getManualScore());
    }

    @Test
    void testConvertToSmallPrompt_AllNull() {
        SmallPromptDto dto = new SmallPromptDto();
        dto.setId(null);
        dto.setName(null);
        dto.setManualScore(null);

        SmallPrompt prompt = new SmallPrompt().convertToSmallPrompt(dto);

        assertNull(prompt.getId());
        assertNull(prompt.getName());
        assertNull(prompt.getManualScore());
    }
}
