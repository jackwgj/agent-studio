/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.common.service.entity.TenantEntity;
import com.openjiuwen.studio.common.service.mapper.TenantMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class TenantRepository {

    @Autowired
    private TenantMapper tenantMapper;

    /**
     * 根据domainId查询激活态的租户信息
     *
     * @param domainId 账户ID
     * @return 账户信息
     */
    public TenantEntity selectById(String domainId) {
        return tenantMapper.selectById(domainId);
    }

    public void insert(TenantEntity tenant) {
        tenantMapper.insert(tenant);
    }

    public void updateById(TenantEntity tenant) {
        tenantMapper.updateById(tenant);
    }
}
