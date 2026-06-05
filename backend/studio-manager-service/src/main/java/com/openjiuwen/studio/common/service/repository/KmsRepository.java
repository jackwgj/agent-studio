/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.common.service.entity.KmsEntity;
import com.openjiuwen.studio.common.service.mapper.KmsMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class KmsRepository {
    @Autowired
    private KmsMapper kmsMapper;

    public List<KmsEntity> selectByDomainId(String domainId, String prefixMainKeyId, String active, String latest) {
        return kmsMapper.selectByDomainId(domainId, prefixMainKeyId, active, latest);
    }
    public void insert(KmsEntity kmsEntity) {
        kmsMapper.insert(kmsEntity);
    }

    public KmsEntity selectById(String id) {
        return kmsMapper.selectById(id);
    }

    public void update(KmsEntity kmsEntity) {
        kmsMapper.update(kmsEntity);
    }
}
