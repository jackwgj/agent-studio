/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.ManualPePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateNewVo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateVo;
import com.openjiuwen.studio.prompt.engineering.dto.TagVo;
import com.openjiuwen.studio.prompt.engineering.utils.DateUtil;
import com.openjiuwen.studio.prompt.engineering.vo.TemplateDownloadVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 模板表
 *
 * @TableName t_pe_prompt_library
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PePromptTemplate implements Serializable {
    /**
     * 模板id
     */
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板来源
     */
    private String source;

    /**
     * 变量
     */
    private String variables;

    /**
     * 变量类型
     */
    private String ptType;

    /**
     * 标签
     */
    private List<PeTag> tags;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改人
     */
    private String updater;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 更新时间
     */
    private Date updatedOn;

    /**
     * 描述
     */
    private String description;

    /**
     * 行业
     */
    private Industry industry;

    private String workspaceId;

    private String projectId;

    private String domainId;

    private int isShare;

    private static final long serialVersionUID = 1L;

    public static PePromptTemplate convertToPePromptTemplate(PePromptTemplateDto dto) {
        PePromptTemplateDTOToPePromptTemplateConverter toPePromptTemplateConverter
            = new PePromptTemplateDTOToPePromptTemplateConverter();
        return toPePromptTemplateConverter.convert(dto);
    }

    public PePromptTemplateVo convertToPePromptTemplateVO() {
        PePromptTemplateToPePromptTemplateVOConverter toPePromptTemplateVOConverter
            = new PePromptTemplateToPePromptTemplateVOConverter();
        return toPePromptTemplateVOConverter.convert(this);
    }

    public PePromptTemplateNewVo convertToPePromptTemplateNewVO() {
        PePromptTemplateToPePromptTemplateNewVOConverter toPePromptTemplateVOConverter = new PePromptTemplateToPePromptTemplateNewVOConverter();
        return toPePromptTemplateVOConverter.convert(this);
    }

    public static PePromptTemplate convertToPePromptTemplate(ManualPePromptTemplateDto dto) {
        ManualPePromptTemplateDTOToPePromptTemplateConverter converter
            = new ManualPePromptTemplateDTOToPePromptTemplateConverter();
        return converter.convert(dto);
    }

    public TemplateDownloadVo convertToTemplateDownloadVo() {
        PePromptTemplateToTemplateDownloadVoConverter toTemplateDownloadVoConverter
            = new PePromptTemplateToTemplateDownloadVoConverter();
        return toTemplateDownloadVoConverter.convert(this);
    }

    private static class PePromptTemplateDTOToPePromptTemplateConverter
        implements FormConvert<PePromptTemplateDto, PePromptTemplate> {
        @Override
        public PePromptTemplate convert(PePromptTemplateDto pePromptTemplateDTO) {
            return PePromptTemplate.builder()
                .id(StringUtils.isEmpty(pePromptTemplateDTO.getId())
                    ? UUID.randomUUID().toString()
                    : pePromptTemplateDTO.getId())
                .name(pePromptTemplateDTO.getName()).content(pePromptTemplateDTO.getContent())
                .source(pePromptTemplateDTO.getSource()).variables(pePromptTemplateDTO.getVariables())
                .industry(StringUtils.isNotEmpty(pePromptTemplateDTO.getIndustryId()) ? Industry.builder()
                    .id(pePromptTemplateDTO.getIndustryId()).build() : new Industry())
                .build();
        }
    }

    private static class ManualPePromptTemplateDTOToPePromptTemplateConverter
        implements FormConvert<ManualPePromptTemplateDto, PePromptTemplate> {
        @Override
        public PePromptTemplate convert(ManualPePromptTemplateDto dto) {
            List<PeTag> newPeTags = new ArrayList<>();
            List<String> tagList = dto.getTagList();
            if(!CollectionUtils.isEmpty(tagList)) {
                tagList.stream().filter(tagId -> !tagId.equals("")).map(tagId -> {
                    PeTag peTag = new PeTag();
                    peTag.setId(tagId);
                    return peTag;
                }).forEach(newPeTags::add);
            }
            return PePromptTemplate.builder()
                .id(StringUtils.isEmpty(dto.getId())
                    ? UUID.randomUUID().toString()
                    : dto.getId())
                .name(dto.getName())
                .content(dto.getContent())
                .source(dto.getSource())
                .variables(dto.getVariables())
                .tags(newPeTags)
                .description(dto.getDescription())
                .industry(Industry.builder().id(dto.getIndustryId()).build())
                .build();
        }
    }

    private static class PePromptTemplateToPePromptTemplateVOConverter
        implements FormConvert<PePromptTemplate, PePromptTemplateVo> {
        @Override
        public PePromptTemplateVo convert(PePromptTemplate pePromptTemplate) {
            List<PeTag> peTags = pePromptTemplate.getTags();
            List<TagVo> tagVos = new ArrayList<>();
            for (PeTag tag : peTags) {
                TagVo tagVo = new TagVo();
                tagVo.setId(tag.getId());
                tagVo.setName(tag.getName());
                tagVo.setNameEn(tag.getNameEn());
                tagVos.add(tagVo);
            }
            PePromptTemplateVo pePromptTemplateVo = new PePromptTemplateVo();
            pePromptTemplateVo.setTemplateId(pePromptTemplate.getId());
            pePromptTemplateVo.setTemplateName(pePromptTemplate.getName());
            pePromptTemplateVo.setContent(pePromptTemplate.getContent());
            pePromptTemplateVo.setVariables(pePromptTemplate.getVariables());
            pePromptTemplateVo.setPtType(pePromptTemplate.getPtType());
            pePromptTemplateVo.setTagList(tagVos);
            pePromptTemplateVo.setCreator(pePromptTemplate.getCreator());
            pePromptTemplateVo.setCreatedOn(DateUtil.GMTFormat(pePromptTemplate.getCreatedOn()));
            pePromptTemplateVo.setUpdatedOn(DateUtil.GMTFormat(pePromptTemplate.getUpdatedOn()));
            pePromptTemplateVo.setDescription(pePromptTemplate.getDescription());
            pePromptTemplateVo.setIndustry(pePromptTemplate.getIndustry() != null
                            ? pePromptTemplate.getIndustry().convertToIndustryVo()
                            : null);
            pePromptTemplateVo.setIsShare(pePromptTemplate.getIsShare());
            return pePromptTemplateVo;
        }
    }

    private static class PePromptTemplateToPePromptTemplateNewVOConverter
        implements FormConvert<PePromptTemplate, PePromptTemplateNewVo> {
        @Override
        public PePromptTemplateNewVo convert(PePromptTemplate pePromptTemplate) {
            List<PeTag> peTags = pePromptTemplate.getTags();
            List<TagVo> tagVos = new ArrayList<>();
            for (PeTag tag : peTags) {
                TagVo tagVo = new TagVo();
                tagVo.setId(tag.getId());
                tagVo.setName(tag.getName());
                tagVo.setNameEn(tag.getNameEn());
                tagVos.add(tagVo);
            }
            PePromptTemplateNewVo pePromptTemplateVo = new PePromptTemplateNewVo();
            pePromptTemplateVo.setId(pePromptTemplate.getId());
            pePromptTemplateVo.setName(pePromptTemplate.getName());
            pePromptTemplateVo.setContent(pePromptTemplate.getContent());
            pePromptTemplateVo.setCreator(pePromptTemplate.getCreator());
            pePromptTemplateVo.setCreatedOn(DateUtil.GMTFormat(pePromptTemplate.getCreatedOn()));
            pePromptTemplateVo.setUpdatedOn(DateUtil.GMTFormat(pePromptTemplate.getUpdatedOn()));
            pePromptTemplateVo.setDescription(pePromptTemplate.getDescription());
            return pePromptTemplateVo;
        }
    }

    private static class PePromptTemplateToTemplateDownloadVoConverter
        implements FormConvert<PePromptTemplate, TemplateDownloadVo> {

        @Override
        public TemplateDownloadVo convert(PePromptTemplate pePromptTemplate) {
            return TemplateDownloadVo.builder()
                .templateName(pePromptTemplate.getName())
                .content(pePromptTemplate.getContent())
                .description(pePromptTemplate.getDescription())
                .variables(pePromptTemplate.getVariables())
                .tag(getTagString(pePromptTemplate))
                .industry(getIndustryName(pePromptTemplate))
                .creator(pePromptTemplate.getCreator())
                .createdOn(getCreatedOn(pePromptTemplate))
                .updatedOn(getUpdatedOn(pePromptTemplate)).build();
        }

        private String getTagString(PePromptTemplate pePromptTemplate) {
            if (Objects.isNull(pePromptTemplate.getTags())) {
                return "";
            }
            return pePromptTemplate.getTags().stream().map(PeTag::getName).collect(Collectors.joining(","));
        }

        private String getIndustryName(PePromptTemplate pePromptTemplate) {
            if (Objects.isNull(pePromptTemplate.getIndustry())) {
                return "";
            }
            return pePromptTemplate.getIndustry().getName();
        }

        private String getCreatedOn(PePromptTemplate pePromptTemplate) {
            if (Objects.isNull(pePromptTemplate.getCreatedOn())) {
                return "";
            }
            return DateUtil.GMTFormat(pePromptTemplate.getCreatedOn());
        }

        private String getUpdatedOn(PePromptTemplate pePromptTemplate) {
            if (Objects.isNull(pePromptTemplate.getUpdatedOn())) {
                return "";
            }
            return DateUtil.GMTFormat(pePromptTemplate.getUpdatedOn());
        }
    }
}
