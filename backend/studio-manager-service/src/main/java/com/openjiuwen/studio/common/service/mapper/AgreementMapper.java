/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.AgreementEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgreementMapper {

    AgreementEntity selectByDomainId(@Param("domainId") String domainId);

    void insert(@Param("entity") AgreementEntity agreementEntity);

    void update(@Param("entity") AgreementEntity agreementEntity);

    List<AgreementEntity> queryList(@Param("domainId") String domainId);
}
