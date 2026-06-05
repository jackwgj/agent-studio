/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.KmsRelationEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KmsRelationMapper {
    List<KmsRelationEntity> selectByDomainId(@Param("domainId") String domainId, @Param("primaryKey") String primaryKey, @Param("type") String type);

    void insert(@Param("param") KmsRelationEntity entity);

    void update(@Param("param") KmsRelationEntity kmsRelationEntity);
}
