/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.TenantConfigEntity;
import com.openjiuwen.studio.common.service.entity.TenantEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantMapper {
    TenantEntity selectById(@Param("domainId") String domainId);

    void insert(@Param("param") TenantEntity tenant);

    void updateById(@Param("param") TenantEntity tenant);

    List<TenantEntity> queryTenantList(@Param("limit") int limit, @Param("offset") int offset);

    List<TenantConfigEntity> queryTenantConfigList();
}
