/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.prompt.engineering.entity.CandidateVariable;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Prompt 相关 DTO 单元测试
 */
class PePromptDtoTest {

    // ========== PePromptDto ==========

    @Test
    void testPePromptDto_GetterSetter() {
        PePromptDto dto = new PePromptDto();
        dto.setId("id-001");
        dto.setName("测试");
        dto.setModel("gpt-4");
        dto.setTaskId("task-001");
        dto.setSource("MODEL_TUNING");
        dto.setType(1);
        dto.setQuestion("问题");
        dto.setAnswer("答案");
        dto.setManualScore(5);
        dto.setFileId("file-001");

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setTemperature(0.7);
        dto.setModelConfig(modelConfig);

        assertEquals("id-001", dto.getId());
        assertEquals("测试", dto.getName());
        assertEquals("gpt-4", dto.getModel());
        assertEquals("task-001", dto.getTaskId());
        assertEquals("MODEL_TUNING", dto.getSource());
        assertEquals(1, dto.getType());
        assertEquals("问题", dto.getQuestion());
        assertEquals("答案", dto.getAnswer());
        assertEquals(5, dto.getManualScore());
        assertEquals("file-001", dto.getFileId());
        assertEquals(0.7, dto.getModelConfig().getTemperature());
    }

    @Test
    void testPePromptDto_NullFields() {
        PePromptDto dto = new PePromptDto();
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getModel());
        assertNull(dto.getVariables());
        assertNull(dto.getModelConfig());
    }

    @Test
    void testPePromptDto_WithVariables() {
        VariableDto var1 = new VariableDto();
        var1.setId("var-001");
        var1.setKey("key1");
        var1.setName("变量1");
        var1.setValue("值1");
        var1.setPeTaskId("task-001");

        PePromptDto dto = new PePromptDto();
        dto.setVariables(List.of(var1));

        assertEquals(1, dto.getVariables().size());
        assertEquals("key1", dto.getVariables().get(0).getKey());
    }

    // ========== VariableDto ==========

    @Test
    void testVariableDto_FullFields() {
        VariableDto dto = new VariableDto();
        dto.setId("var-001");
        dto.setKey("key1");
        dto.setName("变量名");
        dto.setValue("变量值");
        dto.setPeTaskId("task-001");

        assertEquals("var-001", dto.getId());
        assertEquals("key1", dto.getKey());
        assertEquals("变量名", dto.getName());
        assertEquals("变量值", dto.getValue());
        assertEquals("task-001", dto.getPeTaskId());
    }

    // ========== VariableVo ==========

    @Test
    void testVariableVo_FullFields() {
        VariableVo vo = new VariableVo();
        vo.setId("var-001");
        vo.setKey("key1");
        vo.setName("变量名");
        vo.setValue("变量值");
        vo.setPeTaskId("task-001");

        assertEquals("var-001", vo.getId());
        assertEquals("key1", vo.getKey());
        assertEquals("变量名", vo.getName());
        assertEquals("变量值", vo.getValue());
        assertEquals("task-001", vo.getPeTaskId());
    }

    // ========== QueryCondition ==========

    @Test
    void testQueryCondition_FullFields() {
        QueryCondition condition = new QueryCondition();
        condition.setTaskId("task-001");
        condition.setName("搜索名");
        condition.setContent("搜索内容");
        condition.setType(1);
        condition.setIsGoodRating(true);
        condition.setFileId("file-001");

        assertEquals("task-001", condition.getTaskId());
        assertEquals("搜索名", condition.getName());
        assertEquals("搜索内容", condition.getContent());
        assertEquals(1, condition.getType());
        assertTrue(condition.isIsGoodRating());
        assertEquals("file-001", condition.getFileId());
    }

    // ========== UpdateOrDeletePromptCheckDto ==========

    @Test
    void testUpdateOrDeletePromptCheckDto() {
        UpdateOrDeletePromptCheckDto dto = new UpdateOrDeletePromptCheckDto();
        dto.setTaskId("task-001");
        dto.setPromptId("prompt-001");

        assertEquals("task-001", dto.getTaskId());
        assertEquals("prompt-001", dto.getPromptId());
    }

    // ========== SmallPromptDto ==========

    @Test
    void testSmallPromptDto() {
        SmallPromptDto dto = new SmallPromptDto();
        dto.setId("prompt-001");
        dto.setName("小名称");
        dto.setManualScore(10);

        assertEquals("prompt-001", dto.getId());
        assertEquals("小名称", dto.getName());
        assertEquals(10, dto.getManualScore());
    }

    // ========== ModelConfig ==========

    @Test
    void testModelConfig_FullFields() {
        ModelConfig config = new ModelConfig();
        config.setUser("user");
        config.setTemperature(0.8);
        config.setTopP(0.9);
        config.setMaxTokens(4096);
        config.setToken("tk-123");
        config.setPresencePenalty(0.1);
        config.setN(3);
        config.setEndpoint("https://api.example.com");
        config.setProjectId("project-1");
        config.setDeploymentId("deploy-1");
        config.setModelId("model-1");
        config.setModelSource("openai");
        config.setId("cfg-1");

        assertEquals("user", config.getUser());
        assertEquals(0.8, config.getTemperature());
        assertEquals(0.9, config.getTopP());
        assertEquals(4096, config.getMaxTokens());
        assertEquals("tk-123", config.getToken());
        assertEquals(0.1, config.getPresencePenalty());
        assertEquals(3, config.getN());
        assertEquals("https://api.example.com", config.getEndpoint());
        assertEquals("project-1", config.getProjectId());
        assertEquals("deploy-1", config.getDeploymentId());
        assertEquals("model-1", config.getModelId());
        assertEquals("openai", config.getModelSource());
        assertEquals("cfg-1", config.getId());
    }

    // ========== PePromptPageInfo ==========

    @Test
    void testPePromptPageInfo() {
        PePromptVo vo = new PePromptVo();
        vo.setId("p-001");
        vo.setName("测试");

        PePromptPageInfo pageInfo = new PePromptPageInfo();
        pageInfo.setData(List.of(vo));
        pageInfo.setTotalPage(1);
        pageInfo.setCount(1);
        pageInfo.setHasNextPage(false);

        assertEquals(1, pageInfo.getData().size());
        assertEquals(1, pageInfo.getTotalPage());
        assertEquals(1, pageInfo.getCount());
        assertEquals(false, pageInfo.isHasNextPage());
    }

    // ========== FileInfoVo ==========

    @Test
    void testFileInfoVo() {
        FileInfoVo vo = new FileInfoVo();
        vo.setFileId("file-001");
        vo.setTempUrl("https://example.com/temp");

        assertEquals("file-001", vo.getFileId());
        assertEquals("https://example.com/temp", vo.getTempUrl());
    }

    @Test
    void testFileInfoVo_Empty() {
        FileInfoVo vo = new FileInfoVo();
        assertNull(vo.getFileId());
        assertNull(vo.getTempUrl());
    }

    // ========== PePromptVo ==========

    @Test
    void testPePromptVo_FullFields() {
        PePromptVo vo = new PePromptVo();
        vo.setId("p-001");
        vo.setName("Prompt名");
        vo.setContent("内容");
        vo.setAnswer("答案");
        vo.setModel("gpt-4");
        vo.setManualScore(7);
        vo.setCreator("user1");
        vo.setUpdater("user2");
        vo.setEvaluationAvg(85);

        assertEquals("p-001", vo.getId());
        assertEquals("Prompt名", vo.getName());
        assertEquals("内容", vo.getContent());
        assertEquals("答案", vo.getAnswer());
        assertEquals("gpt-4", vo.getModel());
        assertEquals(7, vo.getManualScore());
        assertEquals("user1", vo.getCreator());
        assertEquals("user2", vo.getUpdater());
        assertEquals(85, vo.getEvaluationAvg());
    }

    // ========== JSON round-trip for CandidateVariable ==========

    @Test
    void testCandidateVariableJsonRoundTrip() {
        CandidateVariable cv = new CandidateVariable();
        cv.setId("cv-001");
        cv.setValue("测试值");

        String json = JSON.toJSONString(List.of(cv));
        List<CandidateVariable> parsed = JSON.parseArray(json, CandidateVariable.class);

        assertEquals(1, parsed.size());
        assertEquals("cv-001", parsed.get(0).getId());
        assertEquals("测试值", parsed.get(0).getValue());
    }

    // ========== QueryTemplateCondition ==========

    @Test
    void testQueryTemplateCondition() {
        QueryTemplateCondition condition = new QueryTemplateCondition();
        condition.setLimit(10);
        condition.setOffset(0);
        condition.setTemplateName("模板名");
        condition.setContent("内容");
        condition.setDescription("描述");
        condition.setSource("PRESET");

        assertEquals(10, condition.getLimit());
        assertEquals(0, condition.getOffset());
        assertEquals("模板名", condition.getTemplateName());
        assertEquals("内容", condition.getContent());
        assertEquals("描述", condition.getDescription());
        assertEquals("PRESET", condition.getSource());
    }

    // ========== PePromptTemplateDto ==========

    @Test
    void testPePromptTemplateDto() {
        PePromptTemplateDto dto = new PePromptTemplateDto();
        dto.setId("tmpl-001");
        dto.setName("模板名");
        dto.setContent("内容");
        dto.setSource("PRESET");
        dto.setVariables("变量");
        dto.setTaskId("task-001");
        dto.setIndustryId("industry-001");
        dto.setPromptId("prompt-001");
        dto.setTags(List.of("tag-001"));

        assertEquals("tmpl-001", dto.getId());
        assertEquals("模板名", dto.getName());
        assertEquals("内容", dto.getContent());
        assertEquals("PRESET", dto.getSource());
        assertEquals("变量", dto.getVariables());
        assertEquals("task-001", dto.getTaskId());
        assertEquals("industry-001", dto.getIndustryId());
        assertEquals("prompt-001", dto.getPromptId());
        assertEquals(1, dto.getTags().size());
    }

    // ========== ManualPePromptTemplateDto ==========

    @Test
    void testManualPePromptTemplateDto() {
        ManualPePromptTemplateDto dto = new ManualPePromptTemplateDto();
        dto.setId("tmpl-010");
        dto.setName("手动模板");
        dto.setContent("手动内容");
        dto.setSource("PRESET");
        dto.setVariables("手动变量");
        dto.setTagList(List.of("tag-1", "tag-2"));
        dto.setDescription("描述");
        dto.setIndustryId("industry-010");

        assertEquals("tmpl-010", dto.getId());
        assertEquals("手动模板", dto.getName());
        assertEquals("手动内容", dto.getContent());
        assertEquals("PRESET", dto.getSource());
        assertEquals("手动变量", dto.getVariables());
        assertEquals(2, dto.getTagList().size());
        assertEquals("描述", dto.getDescription());
        assertEquals("industry-010", dto.getIndustryId());
    }

    // ========== PePromptTemplateListVo ==========

    @Test
    void testPePromptTemplateListVo() {
        PePromptTemplateVo vo = new PePromptTemplateVo();
        vo.setTemplateId("tmpl-001");
        vo.setTemplateName("模板名");

        PePromptTemplateListVo listVo = new PePromptTemplateListVo();
        listVo.setData(List.of(vo));
        listVo.setTotalPage(1);
        listVo.setCount(1L);
        listVo.setHasNextPage(false);

        assertEquals(1, listVo.getData().size());
        assertEquals("tmpl-001", listVo.getData().get(0).getTemplateId());
    }

    // ========== PePromptTemplateVo ==========

    @Test
    void testPePromptTemplateVo() {
        PePromptTemplateVo vo = new PePromptTemplateVo();
        vo.setTemplateId("tmpl-001");
        vo.setTemplateName("模板名");
        vo.setContent("内容");
        vo.setVariables("变量");
        vo.setPtType("类型");
        vo.setCreator("user1");
        vo.setDescription("描述");
        vo.setIsShare(1);

        assertEquals("tmpl-001", vo.getTemplateId());
        assertEquals("模板名", vo.getTemplateName());
        assertEquals("内容", vo.getContent());
        assertEquals("变量", vo.getVariables());
        assertEquals("类型", vo.getPtType());
        assertEquals("user1", vo.getCreator());
        assertEquals("描述", vo.getDescription());
        assertEquals(1, vo.getIsShare());
    }
}
