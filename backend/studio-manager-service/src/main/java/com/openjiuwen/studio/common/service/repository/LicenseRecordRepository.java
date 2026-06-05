/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.common.service.entity.LicenseRecordEntity;
import com.openjiuwen.studio.common.service.mapper.LicenseRecordMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class LicenseRecordRepository {

    @Autowired
    private LicenseRecordMapper licenseRecordMapper;

    public void insert(LicenseRecordEntity entity) {
        licenseRecordMapper.insert(entity);
    }
}
