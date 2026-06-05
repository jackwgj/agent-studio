/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper.update;

import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UpdatePromptTemplateMapper {
    List<PePromptTemplate> selectAllTemplates();
}
