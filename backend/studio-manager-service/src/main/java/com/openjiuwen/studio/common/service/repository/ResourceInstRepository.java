/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.repository;

import com.openjiuwen.studio.agent.common.utils.ValidateUtils;
import com.openjiuwen.studio.common.service.constant.CommonConstant;
import com.openjiuwen.studio.common.service.entity.ResourceInstEntity;
import com.openjiuwen.studio.common.service.mapper.ResourceInstMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class ResourceInstRepository {

    @Autowired
    private ResourceInstMapper resourceInstMapper;

    public ResourceInstEntity selectById(String resourceId) {
        return resourceInstMapper.selectById(resourceId);
    }

    public void insert(ResourceInstEntity resourceInstEntity) {
        resourceInstMapper.insert(resourceInstEntity);
    }

    public void updateById(ResourceInstEntity resourceInstEntity) {
        resourceInstMapper.updateById(resourceInstEntity);
    }

    /**
     * 当前租户下，激活状态的sku订购实例
     *
     * @param domainId 租户ID
     * @return 激活的sku实例列表
     */
    public List<ResourceInstEntity> selectActiveResourcesByDomainId(String domainId) {
        if (ValidateUtils.isEmptyString(domainId)) {
            return new ArrayList<>();
        }
        return resourceInstMapper.selectResourcesByDomainId(domainId, CommonConstant.ResourceStatus.ACTIVE);
    }

    public ResourceInstEntity selectActiveResourcesBySkuCode(String skuCode, String domainId) {
        return resourceInstMapper.selectBySkuCode(skuCode, domainId);
    }

    public List<ResourceInstEntity> selectResources(String skuCode, String status, String domainId) {
        return resourceInstMapper.selectResources(skuCode, status, domainId);
    }
}
