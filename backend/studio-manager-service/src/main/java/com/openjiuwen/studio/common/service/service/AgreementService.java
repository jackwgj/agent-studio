/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.service;

import com.openjiuwen.studio.common.service.entity.AgreementEntity;
import com.openjiuwen.studio.common.service.repository.AgreementRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AgreementService {

    @Autowired
    private AgreementRepository agreementRepository;

    public void createAgreement(AgreementEntity entity) {
        agreementRepository.insert(entity);
    }

    public void updateAgreement(AgreementEntity entity) {
        agreementRepository.update(entity);
    }

    public AgreementEntity selectByDomainId(String domainId) {
        return agreementRepository.selectByDomainId(domainId);
    }

    public List<AgreementEntity> queryList(String domainId) {
        return agreementRepository.queryList(domainId);
    }

}
