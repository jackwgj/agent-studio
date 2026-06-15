/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 优化任务相关 DTO 单元测试
 */
class PeOptimizationTaskDtoTest {

    // ========== PeOptimizationTaskDto ==========

    @Test
    void testPeOptimizationTaskDto_FullFields() {
        PeOptimizationTaskDto dto = new PeOptimizationTaskDto();
        dto.setTaskId("task-001");
        dto.setName("优化任务");
        dto.setDescription("描述");
        dto.setType("normal");
        dto.setPromptIds(List.of("prompt-001", "prompt-002"));
        dto.setTestSetId("test-001");

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setDeploymentId("deploy-001");
        dto.setModelConfig(modelConfig);

        ModelConfig assistantConfig = new ModelConfig();
        assistantConfig.setDeploymentId("deploy-002");
        dto.setAssistantConfig(assistantConfig);
        dto.setNumIter(3);

        assertEquals("task-001", dto.getTaskId());
        assertEquals("优化任务", dto.getName());
        assertEquals("描述", dto.getDescription());
        assertEquals("normal", dto.getType());
        assertEquals(2, dto.getPromptIds().size());
        assertEquals("test-001", dto.getTestSetId());
        assertEquals("deploy-001", dto.getModelConfig().getDeploymentId());
        assertEquals("deploy-002", dto.getAssistantConfig().getDeploymentId());
        assertEquals(3, dto.getNumIter());
    }

    @Test
    void testPeOptimizationTaskDto_NullFields() {
        PeOptimizationTaskDto dto = new PeOptimizationTaskDto();
        assertNull(dto.getTaskId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getType());
        assertNull(dto.getTestSetId());
        assertNull(dto.getModelConfig());
        assertNull(dto.getAssistantConfig());
    }

    // ========== PeOptimizationTaskVo ==========

    @Test
    void testPeOptimizationTaskVo_FullFields() {
        PeOptimizationTaskVo vo = new PeOptimizationTaskVo();
        vo.setId("opt-001");
        vo.setName("优化任务");
        vo.setDescription("描述");
        vo.setType("normal");
        vo.setJiuwenTaskId("jiuwen-001");
        vo.setTaskId("task-001");
        vo.setTestSetId("test-001");
        vo.setStatus("running");
        vo.setProgressRate(0.75);
        vo.setBestPrompt("最佳提示词");
        vo.setErrorMsg(null);
        vo.setNumIter(3);
        vo.setCreator("testUser");
        vo.setCreatedOn("2026-01-01");
        vo.setUpdatedOn("2026-01-02");
        vo.setRunningOn("2026-01-01");

        assertEquals("opt-001", vo.getId());
        assertEquals("优化任务", vo.getName());
        assertEquals("描述", vo.getDescription());
        assertEquals("normal", vo.getType());
        assertEquals("jiuwen-001", vo.getJiuwenTaskId());
        assertEquals("task-001", vo.getTaskId());
        assertEquals("test-001", vo.getTestSetId());
        assertEquals("running", vo.getStatus());
        assertEquals(0.75, vo.getProgressRate());
        assertEquals("最佳提示词", vo.getBestPrompt());
        assertNull(vo.getErrorMsg());
        assertEquals(3, vo.getNumIter());
        assertEquals("testUser", vo.getCreator());
    }

    @Test
    void testPeOptimizationTaskVo_NullFields() {
        PeOptimizationTaskVo vo = new PeOptimizationTaskVo();
        assertNull(vo.getId());
        assertNull(vo.getName());
        assertNull(vo.getStatus());
        assertNull(vo.getJiuwenTaskId());
    }

    // ========== TemplateOptimizeReq ==========

    @Test
    void testTemplateOptimizeReq_FullFields() {
        TemplateOptimizeReq req = TemplateOptimizeReq.builder()
            .name("优化任务")
            .desc("描述")
            .rawTemplates(List.of("模板1", "模板2"))
            .optimizeInfo(new OptimizeInfo())
            .modelInfo(new ModelProperties())
            .assistantInfo(new ModelProperties())
            .build();

        assertEquals("优化任务", req.getName());
        assertEquals("描述", req.getDesc());
        assertEquals(2, req.getRawTemplates().size());
        assertNotNull(req.getOptimizeInfo());
        assertNotNull(req.getModelInfo());
        assertNotNull(req.getAssistantInfo());
    }

    // ========== TemplateOptimizeResp ==========

    @Test
    void testTemplateOptimizeResp_WithJobInfo() {
        OptimizationJobInfo jobInfo = new OptimizationJobInfo();
        jobInfo.setId("jiuwen-001");
        jobInfo.setName("优化任务");

        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(200)
            .message("OK")
            .jobInfo(jobInfo)
            .build();

        assertEquals(200, resp.getCode());
        assertEquals("OK", resp.getMessage());
        assertNotNull(resp.getJobInfo());
        assertEquals("jiuwen-001", resp.getJobInfo().getId());
    }

    @Test
    void testTemplateOptimizeResp_WithoutJobInfo() {
        TemplateOptimizeResp resp = TemplateOptimizeResp.builder()
            .code(500)
            .message("error")
            .build();

        assertEquals(500, resp.getCode());
        assertEquals("error", resp.getMessage());
        assertNull(resp.getJobInfo());
    }

    // ========== TemplateOptimizeProgressResp ==========

    @Test
    void testTemplateOptimizeProgressResp_FullFields() {
        ProgressInfo progress = new ProgressInfo();
        TemplateOptimizeProgressResp resp = TemplateOptimizeProgressResp.builder()
            .code(200)
            .message("OK")
            .progress(progress)
            .build();

        assertEquals(200, resp.getCode());
        assertEquals("OK", resp.getMessage());
        assertNotNull(resp.getProgress());
    }
}
