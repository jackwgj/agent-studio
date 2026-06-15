/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptResultVo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptVo;
import com.openjiuwen.studio.prompt.engineering.enums.SourceType;

import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * PePrompt 实体转换器单元测试
 */
class PePromptConverterTest {

    // ========== convertToPePrompt ==========

    @Test
    void testConvertToPePrompt_FullFields() {
        PePromptDto dto = new PePromptDto();
        dto.setId("id-001");
        dto.setName("测试Prompt");
        dto.setModel("gpt-4");
        dto.setTaskId("task-001");
        dto.setSource("MODEL_TUNING");
        dto.setType(1);
        dto.setQuestion("你好");
        dto.setAnswer("你好！");
        dto.setManualScore(5);
        dto.setFileId("file-001");

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setTemperature(0.7);
        dto.setModelConfig(modelConfig);

        PePrompt result = PePrompt.convertToPePrompt(dto);

        assertEquals("id-001", result.getId());
        assertEquals("测试Prompt", result.getName());
        assertEquals("gpt-4", result.getModel());
        assertEquals("task-001", result.getTaskId());
        assertEquals("MODEL_TUNING", result.getSource());
        assertEquals(1, result.getType());
        assertEquals("你好", result.getQuestion());
        assertEquals("你好！", result.getAnswer());
        assertEquals(5, result.getManualScore());
        assertEquals("file-001", result.getFileId());
        assertNotNull(result.getModelConfig());
    }

    @Test
    void testConvertToPePrompt_EmptyIdGeneratesUUID() {
        PePromptDto dto = new PePromptDto();
        dto.setId("");
        dto.setName("无ID");
        dto.setType(SourceType.CANDIDATE.getType());

        PePrompt result = PePrompt.convertToPePrompt(dto);
        assertNotNull(result.getId());
        // UUID 格式校验 (8-4-4-4-12)
        assertEquals(36, result.getId().length());
    }

    @Test
    void testConvertToPePrompt_NullIdGeneratesUUID() {
        PePromptDto dto = new PePromptDto();
        dto.setId(null);
        dto.setName("空ID");
        dto.setType(SourceType.CANDIDATE.getType());

        PePrompt result = PePrompt.convertToPePrompt(dto);
        assertNotNull(result.getId());
    }

    @Test
    void testConvertToPePrompt_NullFields() {
        PePromptDto dto = new PePromptDto();
        dto.setId("id-001");
        dto.setType(SourceType.CANDIDATE.getType());

        PePrompt result = PePrompt.convertToPePrompt(dto);
        assertEquals("id-001", result.getId());
        assertNull(result.getName());
        assertNull(result.getModel());
        assertNull(result.getModelConfig());
    }

    // ========== convertToPePromptVO ==========

    @Test
    void testConvertToPePromptVO_FullFields() {
        PePrompt prompt = PePrompt.builder()
            .id("id-001")
            .name("测试Prompt")
            .question("你是谁？")
            .answer("我是AI助手")
            .model("gpt-4")
            .manualScore(8)
            .creator("user1")
            .updater("user2")
            .createdOn(new Date())
            .updatedOn(new Date())
            .build();

        PePromptVo vo = prompt.convertToPePromptVO();

        assertEquals("id-001", vo.getId());
        assertEquals("测试Prompt", vo.getName());
        assertEquals("你是谁？", vo.getContent());
        assertEquals("我是AI助手", vo.getAnswer());
        assertEquals("gpt-4", vo.getModel());
        assertEquals(8, vo.getManualScore());
        assertEquals("user1", vo.getCreator());
        assertEquals("user2", vo.getUpdater());
        assertNotNull(vo.getCreatedOn());
        assertNotNull(vo.getUpdatedOn());
    }

    @Test
    void testConvertToPePromptVO_MinimalFields() {
        PePrompt prompt = PePrompt.builder()
            .id("id-002")
            .build();

        PePromptVo vo = prompt.convertToPePromptVO();
        assertEquals("id-002", vo.getId());
        assertNull(vo.getName());
        assertNull(vo.getContent());
    }

    // ========== convertToPePromptResultVO ==========

    @Test
    void testConvertToPePromptResultVO_FullFields() {
        PePrompt prompt = PePrompt.builder()
            .id("id-003")
            .name("结果Prompt")
            .question("问题内容")
            .model("gpt-3.5")
            .manualScore(10)
            .creator("admin")
            .updater("admin")
            .createdOn(new Date())
            .updatedOn(new Date())
            .build();

        PePromptResultVo resultVo = prompt.convertToPePromptResultVO();

        assertEquals("id-003", resultVo.getId());
        assertEquals("结果Prompt", resultVo.getName());
        assertEquals("问题内容", resultVo.getContent());
        assertEquals("gpt-3.5", resultVo.getModel());
        assertEquals(10, resultVo.getManualScore());
    }

    @Test
    void testConvertToPePromptResultVO_MinimalFields() {
        PePrompt prompt = PePrompt.builder()
            .id("id-004")
            .build();

        PePromptResultVo resultVo = prompt.convertToPePromptResultVO();
        assertEquals("id-004", resultVo.getId());
        assertNull(resultVo.getName());
    }
}
