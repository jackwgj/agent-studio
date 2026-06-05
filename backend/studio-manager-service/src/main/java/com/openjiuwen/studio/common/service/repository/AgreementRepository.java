/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.common.service.entity.AgreementEntity;
import com.openjiuwen.studio.common.service.mapper.AgreementMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class AgreementRepository {
    @Autowired
    private AgreementMapper agreementMapper;

    public AgreementEntity selectByDomainId(String domainId) {
        return agreementMapper.selectByDomainId(domainId);
    }

    public List<AgreementEntity> queryList(String domainId) {
        return agreementMapper.queryList(domainId);
    }

    public void insert(AgreementEntity agreementEntity) {
        agreementMapper.insert(agreementEntity);
    }

    public void update(AgreementEntity agreementEntity) {
        agreementMapper.update(agreementEntity);
    }

}
