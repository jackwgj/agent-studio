/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper;

import com.openjiuwen.studio.agent.runtime.entity.ReleaseChannel;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发布通道 数据库操作mapper
 *
 */
@Mapper
public interface ChannelReleaseMapper {

    /**
     * 根据应用ID、项目ID和工作空间ID查询发布渠道列表。
     *
     * @param appId 应用的唯一标识符。
     * @param projectId 项目的唯一标识符。
     * @param workspaceId 工作空间的唯一标识符。
     * @return 符合条件的发布渠道列表。
     */
    List<ReleaseChannel> selectByAppIdAndShortCodeAndWorkspaceId(@Param("appId") String appId,
        @Param("shortCode") String shortCode, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

}
