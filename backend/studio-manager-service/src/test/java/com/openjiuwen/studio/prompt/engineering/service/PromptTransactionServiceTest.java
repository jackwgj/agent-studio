/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.openjiuwen.studio.prompt.engineering.entity.Industry;
import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;
import com.openjiuwen.studio.prompt.engineering.entity.PeTag;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

/**
 * PromptTransactionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PromptTransactionServiceTest {

    @InjectMocks
    private PromptTransactionService promptTransactionService;

    @Mock
    private PePromptTemplateMapper pePromptTemplateMapper;

    // ========== insertOneTemplate ==========

    @Test
    void testInsertOneTemplate_WithTags() {
        PePromptTemplate template = new PePromptTemplate();
        template.setId("tmpl-001");
        template.setWorkspaceId("workspace1");
        Industry industry = new Industry();
        industry.setId("industry-001");
        template.setIndustry(industry);

        List<String> tagIds = List.of("tag-001", "tag-002");
        String projectId = "project1";

        promptTransactionService.insertOneTemplate(template, tagIds, projectId);

        verify(pePromptTemplateMapper).insertOneTemplate(template, projectId, "industry-001");
        verify(pePromptTemplateMapper).bindingTemplateIdAndTagId("tmpl-001", tagIds, "workspace1");
    }

    @Test
    void testInsertOneTemplate_WithoutTags() {
        PePromptTemplate template = new PePromptTemplate();
        template.setId("tmpl-002");
        template.setWorkspaceId("workspace1");
        Industry industry = new Industry();
        industry.setId("industry-002");
        template.setIndustry(industry);

        String projectId = "project1";

        promptTransactionService.insertOneTemplate(template, Collections.emptyList(), projectId);

        verify(pePromptTemplateMapper).insertOneTemplate(template, projectId, "industry-002");
        verify(pePromptTemplateMapper, never()).bindingTemplateIdAndTagId(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testInsertOneTemplate_NullTags() {
        PePromptTemplate template = new PePromptTemplate();
        template.setId("tmpl-003");
        template.setWorkspaceId("workspace1");
        Industry industry = new Industry();
        industry.setId("industry-003");
        template.setIndustry(industry);

        String projectId = "project1";

        promptTransactionService.insertOneTemplate(template, null, projectId);

        verify(pePromptTemplateMapper).insertOneTemplate(template, projectId, "industry-003");
        verify(pePromptTemplateMapper, never()).bindingTemplateIdAndTagId(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyList(),
            org.mockito.ArgumentMatchers.anyString());
    }

    // ========== batchInsertTemplate ==========

    @Test
    void testBatchInsertTemplate_WithTags() {
        PeTag tag = new PeTag();
        tag.setId("tag-001");

        PePromptTemplate template1 = new PePromptTemplate();
        template1.setId("tmpl-001");
        template1.setWorkspaceId("workspace1");
        Industry industry1 = new Industry();
        industry1.setId("industry-001");
        template1.setIndustry(industry1);
        template1.setTags(List.of(tag));

        PePromptTemplate template2 = new PePromptTemplate();
        template2.setId("tmpl-002");
        template2.setWorkspaceId("workspace1");
        Industry industry2 = new Industry();
        industry2.setId("industry-002");
        template2.setIndustry(industry2);
        template2.setTags(Collections.emptyList());

        String projectId = "project1";

        promptTransactionService.batchInsertTemplate(List.of(template1, template2), projectId);

        verify(pePromptTemplateMapper).insertOneTemplate(template1, projectId, "industry-001");
        verify(pePromptTemplateMapper).insertOneTemplate(template2, projectId, "industry-002");
        verify(pePromptTemplateMapper).bindingTemplateIdAndTagId("tmpl-001", List.of("tag-001"), "workspace1");
    }

    @Test
    void testBatchInsertTemplate_EmptyList() {
        promptTransactionService.batchInsertTemplate(Collections.emptyList(), "project1");

        verify(pePromptTemplateMapper, never()).insertOneTemplate(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testBatchInsertTemplate_NullIndustry() {
        PePromptTemplate template = new PePromptTemplate();
        template.setId("tmpl-004");
        template.setWorkspaceId("workspace1");
        Industry industry = new Industry();
        industry.setId("industry-004");
        template.setIndustry(industry);
        template.setTags(Collections.emptyList());

        promptTransactionService.batchInsertTemplate(List.of(template), "project1");

        verify(pePromptTemplateMapper).insertOneTemplate(template, "project1", "industry-004");
    }
}
