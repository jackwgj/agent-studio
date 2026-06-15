/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.prompt.engineering.dto.ManualPePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateNewVo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateVo;
import com.openjiuwen.studio.prompt.engineering.vo.TemplateDownloadVo;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

/**
 * PePromptTemplate 实体转换器单元测试
 */
class PePromptTemplateConverterTest {

    // ========== convertToPePromptTemplate(PePromptTemplateDto) ==========

    @Test
    void testConvertFromDto_FullFields() {
        PePromptTemplateDto dto = new PePromptTemplateDto();
        dto.setId("tmpl-001");
        dto.setName("测试模板");
        dto.setContent("模板内容");
        dto.setSource("PRESET");
        dto.setVariables("变量1");
        dto.setIndustryId("industry-001");

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);

        assertEquals("tmpl-001", result.getId());
        assertEquals("测试模板", result.getName());
        assertEquals("模板内容", result.getContent());
        assertEquals("PRESET", result.getSource());
        assertEquals("变量1", result.getVariables());
        assertNotNull(result.getIndustry());
        assertEquals("industry-001", result.getIndustry().getId());
    }

    @Test
    void testConvertFromDto_EmptyIdGeneratesUUID() {
        PePromptTemplateDto dto = new PePromptTemplateDto();
        dto.setId("");
        dto.setName("无ID模板");

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        assertNotNull(result.getId());
        assertEquals(36, result.getId().length());
    }

    @Test
    void testConvertFromDto_NullIndustryId() {
        PePromptTemplateDto dto = new PePromptTemplateDto();
        dto.setId("tmpl-002");
        dto.setName("无行业");
        dto.setIndustryId(null);

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        assertNotNull(result.getIndustry());
    }

    @Test
    void testConvertFromDto_EmptyIndustryId() {
        PePromptTemplateDto dto = new PePromptTemplateDto();
        dto.setId("tmpl-003");
        dto.setName("空行业");
        dto.setIndustryId("");

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        assertNotNull(result.getIndustry());
    }

    // ========== convertToPePromptTemplate(ManualPePromptTemplateDto) ==========

    @Test
    void testConvertFromManualDto_FullFields() {
        ManualPePromptTemplateDto dto = new ManualPePromptTemplateDto();
        dto.setId("tmpl-010");
        dto.setName("手动模板");
        dto.setContent("手动内容");
        dto.setSource("PRESET");
        dto.setVariables("手动变量");
        dto.setDescription("描述信息");
        dto.setIndustryId("industry-010");
        dto.setTagList(List.of("tag-001", "tag-002"));

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);

        assertEquals("tmpl-010", result.getId());
        assertEquals("手动模板", result.getName());
        assertEquals("手动内容", result.getContent());
        assertEquals("PRESET", result.getSource());
        assertEquals("手动变量", result.getVariables());
        assertEquals("描述信息", result.getDescription());
        assertNotNull(result.getIndustry());
        assertEquals("industry-010", result.getIndustry().getId());
        assertEquals(2, result.getTags().size());
        assertEquals("tag-001", result.getTags().get(0).getId());
        assertEquals("tag-002", result.getTags().get(1).getId());
    }

    @Test
    void testConvertFromManualDto_EmptyIdGeneratesUUID() {
        ManualPePromptTemplateDto dto = new ManualPePromptTemplateDto();
        dto.setId("");
        dto.setName("无ID");

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        assertNotNull(result.getId());
    }

    @Test
    void testConvertFromManualDto_EmptyTagList() {
        ManualPePromptTemplateDto dto = new ManualPePromptTemplateDto();
        dto.setId("tmpl-011");
        dto.setName("无标签");
        dto.setTagList(List.of());

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void testConvertFromManualDto_NullTagList() {
        ManualPePromptTemplateDto dto = new ManualPePromptTemplateDto();
        dto.setId("tmpl-012");
        dto.setName("空标签");
        dto.setTagList(null);

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void testConvertFromManualDto_TagListWithEmptyString() {
        ManualPePromptTemplateDto dto = new ManualPePromptTemplateDto();
        dto.setId("tmpl-013");
        dto.setName("含空标签");
        dto.setTagList(List.of("tag-001", "", "tag-002"));

        PePromptTemplate result = PePromptTemplate.convertToPePromptTemplate(dto);
        // 空字符串tagId应该被过滤
        assertEquals(2, result.getTags().size());
    }

    // ========== convertToPePromptTemplateVO ==========

    @Test
    void testConvertToVo_FullFields() {
        PeTag tag = new PeTag();
        tag.setId("tag-001");
        tag.setName("标签1");
        tag.setNameEn("Tag1");

        Industry industry = new Industry();
        industry.setId("industry-001");
        industry.setName("行业1");

        PePromptTemplate template = PePromptTemplate.builder()
            .id("tmpl-001")
            .name("测试模板")
            .content("模板内容")
            .variables("变量1")
            .ptType("type1")
            .tags(List.of(tag))
            .creator("user1")
            .updater("user2")
            .createdOn(new Date())
            .updatedOn(new Date())
            .description("描述")
            .industry(industry)
            .isShare(0)
            .build();

        PePromptTemplateVo vo = template.convertToPePromptTemplateVO();

        assertEquals("tmpl-001", vo.getTemplateId());
        assertEquals("测试模板", vo.getTemplateName());
        assertEquals("模板内容", vo.getContent());
        assertEquals("变量1", vo.getVariables());
        assertEquals("type1", vo.getPtType());
        assertEquals(1, vo.getTagList().size());
        assertEquals("tag-001", vo.getTagList().get(0).getId());
        assertEquals("标签1", vo.getTagList().get(0).getName());
        assertEquals("Tag1", vo.getTagList().get(0).getNameEn());
        assertEquals("user1", vo.getCreator());
        assertEquals("描述", vo.getDescription());
        assertNotNull(vo.getCreatedOn());
        assertNotNull(vo.getUpdatedOn());
        assertNotNull(vo.getIndustry());
        assertEquals(0, vo.getIsShare());
    }

    @Test
    void testConvertToVo_EmptyTags() {
        PePromptTemplate template = PePromptTemplate.builder()
            .id("tmpl-002")
            .name("无标签")
            .tags(List.of())
            .createdOn(new Date())
            .updatedOn(new Date())
            .build();

        PePromptTemplateVo vo = template.convertToPePromptTemplateVO();
        assertEquals("tmpl-002", vo.getTemplateId());
        assertTrue(vo.getTagList().isEmpty());
    }

    @Test
    void testConvertToVo_NullIndustry() {
        PePromptTemplate template = PePromptTemplate.builder()
            .id("tmpl-003")
            .name("无行业")
            .tags(List.of())
            .industry(null)
            .createdOn(new Date())
            .updatedOn(new Date())
            .build();

        PePromptTemplateVo vo = template.convertToPePromptTemplateVO();
        assertNull(vo.getIndustry());
    }

    // ========== convertToPePromptTemplateNewVO ==========

    @Test
    void testConvertToNewVo_FullFields() {
        PeTag tag = new PeTag();
        tag.setId("tag-001");
        tag.setName("标签1");

        PePromptTemplate template = PePromptTemplate.builder()
            .id("tmpl-100")
            .name("新VO模板")
            .content("内容")
            .creator("user1")
            .createdOn(new Date())
            .updatedOn(new Date())
            .description("描述")
            .tags(List.of(tag))
            .build();

        PePromptTemplateNewVo newVo = template.convertToPePromptTemplateNewVO();
        assertEquals("tmpl-100", newVo.getId());
        assertEquals("新VO模板", newVo.getName());
        assertEquals("内容", newVo.getContent());
        assertEquals("user1", newVo.getCreator());
        assertEquals("描述", newVo.getDescription());
    }

    // ========== convertToTemplateDownloadVo ==========

    @Test
    void testConvertToTemplateDownloadVo_FullFields() {
        PeTag tag1 = new PeTag();
        tag1.setName("标签1");
        PeTag tag2 = new PeTag();
        tag2.setName("标签2");

        Industry industry = new Industry();
        industry.setName("行业1");

        PePromptTemplate template = PePromptTemplate.builder()
            .name("下载模板")
            .content("模板内容")
            .description("描述")
            .variables("变量1")
            .tags(List.of(tag1, tag2))
            .industry(industry)
            .creator("user1")
            .createdOn(new Date())
            .updatedOn(new Date())
            .build();

        TemplateDownloadVo downloadVo = template.convertToTemplateDownloadVo();
        assertEquals("下载模板", downloadVo.getTemplateName());
        assertEquals("模板内容", downloadVo.getContent());
        assertEquals("描述", downloadVo.getDescription());
        assertEquals("变量1", downloadVo.getVariables());
        assertEquals("标签1,标签2", downloadVo.getTag());
        assertEquals("行业1", downloadVo.getIndustry());
        assertEquals("user1", downloadVo.getCreator());
        assertNotNull(downloadVo.getCreatedOn());
        assertNotNull(downloadVo.getUpdatedOn());
    }

    @Test
    void testConvertToTemplateDownloadVo_NullTags() {
        PePromptTemplate template = PePromptTemplate.builder()
            .name("无标签模板")
            .tags(null)
            .industry(null)
            .build();

        TemplateDownloadVo downloadVo = template.convertToTemplateDownloadVo();
        assertEquals("", downloadVo.getTag());
        assertEquals("", downloadVo.getIndustry());
    }

    @Test
    void testConvertToTemplateDownloadVo_NullDates() {
        PePromptTemplate template = PePromptTemplate.builder()
            .name("无日期模板")
            .tags(List.of())
            .createdOn(null)
            .updatedOn(null)
            .build();

        TemplateDownloadVo downloadVo = template.convertToTemplateDownloadVo();
        assertEquals("", downloadVo.getCreatedOn());
        assertEquals("", downloadVo.getUpdatedOn());
    }
}
