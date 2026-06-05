/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.ResourceInstEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceInstMapper {
    ResourceInstEntity selectById(@Param("resourceId") String resourceId);

    void insert(@Param("param") ResourceInstEntity resourceInstEntity);

    void updateById(@Param("param") ResourceInstEntity resourceInstEntity);

    List<ResourceInstEntity> selectResourcesByDomainId(@Param("domainId") String domainId, @Param("status") String status);

    ResourceInstEntity selectBySkuCode(@Param("skuCode") String skuCode, @Param("domainId") String domainId);

    List<ResourceInstEntity> selectResources(@Param("skuCode") String skuCode, @Param("status") String status, @Param("domainId") String domainId);
}
