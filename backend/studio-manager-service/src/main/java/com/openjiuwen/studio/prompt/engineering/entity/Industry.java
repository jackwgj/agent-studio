/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.IndustryDto;
import com.openjiuwen.studio.prompt.engineering.dto.IndustryVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Industry implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键UUID
     */
    private String id;

    /**
     * 名称/值，唯一
     */
    private String name;

    /**
     * 英文行业名称
     */
    private String nameEn;

    /**
     * 行业描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 更新时间
     */
    private Date updatedOn;

    private String libraryType;

    private String workspaceId;

    public static Industry convertToIndustry(IndustryDto dto, String workspaceId) {
        IndustryDtoToIndustryConverter converter = new IndustryDtoToIndustryConverter();
        Industry convert = converter.convert(dto);
        convert.setWorkspaceId(workspaceId);
        return convert;
    }

    public IndustryVo convertToIndustryVo() {
        IndustryToIndustryVoConverter converter = new IndustryToIndustryVoConverter();
        return converter.convert(this);
    }

    private static class IndustryDtoToIndustryConverter implements FormConvert<IndustryDto, Industry> {
        @Override
        public Industry convert(IndustryDto industryDto) {
            return Industry.builder()
                    .id(StringUtils.isEmpty(industryDto.getId()) ? UUID.randomUUID().toString() : industryDto.getId())
                    .name(industryDto.getName())
                .nameEn(industryDto.getNameEn())
                    .description(industryDto.getDescription())
                    .build();
        }
    }

    private static class IndustryToIndustryVoConverter implements FormConvert<Industry, IndustryVo> {
        @Override
        public IndustryVo convert(Industry industry) {
            IndustryVo industryVo = new IndustryVo();
            industryVo.setId(industry.getId());
            industryVo.setName(industry.getName());
            industryVo.setNameEn(industry.getNameEn());
            industryVo.setDescription(industry.getDescription());
            industryVo.setCreatedOn(industry.getCreatedOn());
            industryVo.setUpdatedOn(industry.getUpdatedOn());
            industryVo.setLibraryType((industry.getLibraryType()));
            return industryVo;
        }
    }
}
