/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.ToolEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 工具Mapper
 *
 */
@Mapper
public interface OldPluginMapper {

    ToolEntity selectOldPluginById(@Param("toolId") String toolId);

}
