/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.LicenseRecordEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LicenseRecordMapper {

    void insert(@Param("param") LicenseRecordEntity entity);
}
