/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.agent.common.utils.ValidateUtils;
import com.openjiuwen.studio.common.service.entity.LicenseEntity;
import com.openjiuwen.studio.common.service.mapper.LicenseMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class LicenseRepository {

    @Autowired
    private LicenseMapper licenseMapper;

    public void updateById(LicenseEntity licenseEntity) {
        licenseMapper.updateById(licenseEntity);
    }

    public void batchInsert(List<LicenseEntity> licenseEntityList) {
        if (ValidateUtils.isEmptyCollection(licenseEntityList)) {
            return;
        }
        licenseMapper.batchInsert(licenseEntityList);
    }

    public void insert(LicenseEntity licenseEntity) {
        licenseMapper.insert(licenseEntity);
    }

    public void batchUpdate(List<LicenseEntity> updateLicenseList) {
        if (ValidateUtils.isEmptyCollection(updateLicenseList)) {
            return;
        }
        licenseMapper.batchUpdate(updateLicenseList);
    }

    public List<LicenseEntity> selectByResourceId(String resourceId, String domainId) {
        return licenseMapper.selectByResourceId(resourceId, domainId);
    }

    public List<LicenseEntity> selectByConditions(String resourceId, String attrCode, String skuCode, String domainId, String status) {
        return licenseMapper.selectByConditions(resourceId, attrCode, skuCode, domainId, status);
    }
}
