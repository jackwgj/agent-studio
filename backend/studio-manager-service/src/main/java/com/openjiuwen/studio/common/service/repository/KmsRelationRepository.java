/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.common.service.entity.KmsRelationEntity;
import com.openjiuwen.studio.common.service.mapper.KmsRelationMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class KmsRelationRepository {
    @Autowired
    private KmsRelationMapper kmsRelationMapper;

    public List<KmsRelationEntity> selectByDomainId(String domainId, String primaryKey, String type) {
        return kmsRelationMapper.selectByDomainId(domainId, primaryKey, type);
    }
    public void insert(KmsRelationEntity entity) {
        kmsRelationMapper.insert(entity);
    }


    public void update(KmsRelationEntity kmsRelationEntity) {
        kmsRelationMapper.update(kmsRelationEntity);
    }
}
