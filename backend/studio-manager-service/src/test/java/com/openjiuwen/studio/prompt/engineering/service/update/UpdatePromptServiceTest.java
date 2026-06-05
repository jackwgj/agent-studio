/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.prompt.engineering.entity.Industry;
import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.update.UpdatePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.service.stratup.UpdatePromptService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class UpdatePromptServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PePromptTemplateMapper templateMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpdatePromptTemplateMapper updatePromptTemplateMapper;

    @InjectMocks
    private UpdatePromptService updatePromptService;

    private AutoCloseable mockitoCloseable;


    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_updatePrompt_success() throws Exception {
        // 准备测试数据
        PePromptTemplate template1 = new PePromptTemplate();
        template1.setId("1");
        template1.setName("模板1");
        template1.setProjectId("project1");
        template1.setWorkspaceId("old_workspace1");
        template1.setIndustry(new Industry());
        template1.getIndustry().setId("industry1");

        PePromptTemplate template2 = new PePromptTemplate();
        template2.setId("2");
        template2.setName("模板2");
        template2.setProjectId("project2");
        template2.setWorkspaceId("old_workspace2");
        template2.setIndustry(new Industry());
        template2.getIndustry().setId("industry2");

        List<PePromptTemplate> promptOld = Arrays.asList(template1, template2);

        List<String> existingIds = List.of("1"); // id 为 1 的模板已存在

        // 模拟方法返回值
        when(updatePromptTemplateMapper.selectAllTemplates()).thenReturn(promptOld);
        when(templateMapper.selectAllIds()).thenReturn(existingIds);
        doNothing().when(templateMapper).insertOneTemplate(any(PePromptTemplate.class), anyString(), anyString());

        // 执行方法
        updatePromptService.updatePrompt();
    }

}
