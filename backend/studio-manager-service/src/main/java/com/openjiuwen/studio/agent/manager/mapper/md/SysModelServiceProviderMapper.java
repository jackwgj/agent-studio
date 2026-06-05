/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.md;

import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProviderDetail;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderCondition;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysModelServiceProviderMapper {
    // 根据ID查询
    ModelServiceProvider selectById(@Param("id") String id);

    List<ModelServiceProviderDetail> queryByCondition(@Param("condition") ProviderCondition condition,
        @Param("limit") int limit, @Param("offset") int offset);
}
