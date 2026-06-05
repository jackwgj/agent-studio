/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.entity.ShareResourcePromptEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShareResourcePromptMapper {
    ShareResourcePromptEntity selectShareResourceEntityById(@Param("resourceId") String resourceId,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);
}
