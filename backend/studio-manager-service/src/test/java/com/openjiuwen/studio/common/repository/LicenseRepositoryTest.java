/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.repository;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.openjiuwen.studio.common.service.entity.LicenseRecordEntity;
import com.openjiuwen.studio.common.service.mapper.LicenseRecordMapper;

import com.openjiuwen.studio.common.service.repository.LicenseRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LicenseRecordRepositoryTest {

    @Mock
    private LicenseRecordMapper licenseRecordMapper;

    @InjectMocks
    private LicenseRecordRepository licenseRecordRepository;

    @Test
    void insert_shouldDelegateToMapper() {
        // given
        LicenseRecordEntity entity = new LicenseRecordEntity();

        // when
        licenseRecordRepository.insert(entity);

        // then
        verify(licenseRecordMapper, times(1)).insert(entity);
    }
}
