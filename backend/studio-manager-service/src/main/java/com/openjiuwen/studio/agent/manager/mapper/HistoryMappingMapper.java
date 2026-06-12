/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.HistoryMappingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HistoryMappingMapper {

    List<HistoryMappingEntity> findByAppId(@Param("appId") String appId);

    int deleteByAppId(@Param("appId") String appId);

    int insert(HistoryMappingEntity entity);

    int insertBatch(@Param("list") List<HistoryMappingEntity> list);
}
