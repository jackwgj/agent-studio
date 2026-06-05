/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.mapper;

import com.openjiuwen.studio.common.service.entity.LicenseEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LicenseMapper {

    void batchInsert(@Param("list") List<LicenseEntity> licenseEntityList);

    void insert(@Param("licenseEntity") LicenseEntity licenseEntity);

    void batchUpdate(@Param("list") List<LicenseEntity> updateLicenseList);

    void updateById(@Param("param") LicenseEntity licenseEntity);

    List<LicenseEntity> selectByResourceId(@Param("resourceId") String resourceId, @Param("domainId") String domainId);

    List<LicenseEntity> selectByConditions(@Param("resourceId") String resourceId, @Param("attrCode") String attrCode,
                                           @Param("skuCode") String skuCode, @Param("domainId") String domainId,
                                           @Param("status") String status);
}
