/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.dto.IndustryDto;
import com.openjiuwen.studio.prompt.engineering.entity.Industry;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PeIndustryMapper {
    Boolean isExist(@Param("body") IndustryDto body, @Param("workspaceId") String workspaceId);

    void insertOne(Industry industry);

    List<Industry> queryAll();

    List<Industry> queryIndustryByLibraryType(@Param("libraryType") String libraryType);

    Integer deleteById(String industryId, String workspaceId);

    Integer updateOne(Industry industry);
}
