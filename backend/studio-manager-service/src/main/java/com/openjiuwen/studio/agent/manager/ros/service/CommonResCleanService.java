/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.service;

import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.mapper.ros.ResourceCleanMapper;
import com.openjiuwen.studio.agent.manager.ros.relation.IRelateSourceCleaner;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CommonResCleanService {

    @Autowired
    private ResourceCleanMapper dataCleanMapper;

    /**
     * 根据domain清理
     */
    public List<String> cleanByDomain(CleanResource cleanResource, String domainId) {
        // 1.查询资源
        List<String> ids = dataCleanMapper.selectByDomain(cleanResource.getTableName(), cleanResource.getIdColumn(),
            cleanResource.getDomainColumn(), domainId);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        // 2.删除相关资源
        IRelateSourceCleaner relSrcCleaner = null;
        try {
            relSrcCleaner = SpringBeanUtils.getBean(cleanResource.getResource() + "_RelateResourceCleaner",
                IRelateSourceCleaner.class);
        } catch (Exception e) {
            log.info("Resource:{} has no relation data.", cleanResource.getResource());
        }
        if (relSrcCleaner != null) {
            relSrcCleaner.clean(ids, domainId);
        }

        // 3.删除资源本身
        dataCleanMapper.batchDeleteByIds(cleanResource.getTableName(), cleanResource.getIdColumn(), ids);
        return ids;
    }
}
