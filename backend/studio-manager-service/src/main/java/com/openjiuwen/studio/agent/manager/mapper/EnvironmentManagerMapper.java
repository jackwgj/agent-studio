/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.EnvironmentManagerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工具Mapper
 *
 */
@Mapper
public interface EnvironmentManagerMapper {


    List<EnvironmentManagerEntity> selectAll(@Param("projectId") String projectId, @Param("pageOffset") int pageOffset, @Param("pageSize") int pageSize);

    List<EnvironmentManagerEntity> selectAllByStatus(@Param("status")String status, @Param("projectId") String projectId, @Param("pageOffset") int pageOffset, @Param("pageSize") int pageSize);

}
