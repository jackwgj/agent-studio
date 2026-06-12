/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.HistoryReleaseVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HistoryReleaseVersionMapper {

    HistoryReleaseVersionEntity findByAppIdAndVersionId(
            @Param("appId") String appId,
            @Param("versionId") String versionId
    );

    int deleteByAppId(@Param("appId") String appId);

    int insert(HistoryReleaseVersionEntity entity);

    int insertBatch(@Param("list") List<HistoryReleaseVersionEntity> list);
}
