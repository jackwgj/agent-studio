/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.KmsEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KmsMapper {
    List<KmsEntity> selectByDomainId(@Param("domainId") String domainId, @Param("mainKeyId") String mainKeyId,
        @Param("active") String active, @Param("latest") String latest);

    void insert(@Param("param") KmsEntity kmsEntity);

    KmsEntity selectById(@Param("id") String id);

    void update(@Param("param") KmsEntity kmsEntity);
}
