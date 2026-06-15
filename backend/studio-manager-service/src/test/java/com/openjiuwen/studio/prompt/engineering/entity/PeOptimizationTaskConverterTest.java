/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PeOptimizationTaskVo;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

/**
 * PeOptimizationTask 实体转换器单元测试
 */
class PeOptimizationTaskConverterTest {

    // ========== convertToPeOptimizationTaskVo ==========

    @Test
    void testConvertToPeOptimizationTaskVo_FullFields() {
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setDeploymentId("deploy-001");
        modelConfig.setToken("secret-token");

        ModelConfig assistantConfig = new ModelConfig();
        assistantConfig.setDeploymentId("deploy-002");
        assistantConfig.setToken("assistant-secret");

        PePrompt prompt = new PePrompt();
        prompt.setId("prompt-001");
        prompt.setName("测试Prompt");
        prompt.setVariables("[]");

        PeOptimizationTask task = PeOptimizationTask.builder()
            .id("opt-001")
            .projectId("project1")
            .taskId("task-001")
            .name("优化任务")
            .description("描述信息")
            .type("normal")
            .jiuwenTaskId("jiuwen-001")
            .prompts(Collections.singletonList(prompt))
            .testSetId("test-001")
            .modelConfig(modelConfig)
            .assistantConfig(assistantConfig)
            .numIter(3)
            .bestPrompt("最佳提示词")
            .errorMsg(null)
            .progressRate(0.5)
            .status(OptimizationTaskStatus.RUNNING)
            .creator("testUser")
            .createdOn(new Date())
            .updatedOn(new Date())
            .runningOn(new Date())
            .workspaceId("workspace1")
            .build();

        PeOptimizationTaskVo vo = task.convertToPeOptimizationTaskVo();

        assertEquals("opt-001", vo.getId());
        assertEquals("优化任务", vo.getName());
        assertEquals("描述信息", vo.getDescription());
        assertEquals("normal", vo.getType());
        assertEquals("jiuwen-001", vo.getJiuwenTaskId());
        assertEquals("task-001", vo.getTaskId());
        assertEquals("test-001", vo.getTestSetId());
        assertNotNull(vo.getModelConfig());
        assertNull(vo.getModelConfig().getToken());
        assertNotNull(vo.getAssistantConfig());
        assertNull(vo.getAssistantConfig().getToken());
        assertEquals(3, vo.getNumIter());
        assertEquals("running", vo.getStatus());
        assertEquals(0.5, vo.getProgressRate());
        assertEquals("最佳提示词", vo.getBestPrompt());
        assertEquals("testUser", vo.getCreator());
        assertNotNull(vo.getCreatedOn());
        assertNotNull(vo.getUpdatedOn());
        assertNotNull(vo.getRunningOn());
        assertEquals(1, vo.getPrompts().size());
    }

    @Test
    void testConvertToPeOptimizationTaskVo_MinimalFields() {
        PeOptimizationTask task = new PeOptimizationTask();
        task.setId("opt-002");
        task.setStatus(OptimizationTaskStatus.PENDING);
        task.setPrompts(Collections.emptyList());
        task.setModelConfig(null);
        task.setAssistantConfig(null);
        task.setCreatedOn(new Date());
        task.setUpdatedOn(new Date());
        task.setRunningOn(new Date());

        PeOptimizationTaskVo vo = task.convertToPeOptimizationTaskVo();

        assertEquals("opt-002", vo.getId());
        assertNull(vo.getName());
        assertNull(vo.getModelConfig());
        assertNull(vo.getAssistantConfig());
        assertEquals("pending", vo.getStatus());
    }

    @Test
    void testConvertToPeOptimizationTaskVo_TokenCleared() {
        ModelConfig config = new ModelConfig();
        config.setToken("secret-token");
        config.setDeploymentId("deploy-001");

        PeOptimizationTask task = new PeOptimizationTask();
        task.setId("opt-003");
        task.setStatus(OptimizationTaskStatus.SUCCESS);
        task.setModelConfig(config);
        task.setPrompts(Collections.emptyList());
        task.setCreatedOn(new Date());
        task.setUpdatedOn(new Date());
        task.setRunningOn(new Date());

        PeOptimizationTaskVo vo = task.convertToPeOptimizationTaskVo();

        assertNull(vo.getModelConfig().getToken());
        assertEquals("deploy-001", vo.getModelConfig().getDeploymentId());
    }
}
