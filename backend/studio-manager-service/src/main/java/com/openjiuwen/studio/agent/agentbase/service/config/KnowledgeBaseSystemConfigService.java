/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseSystemConfigMapper;
import com.openjiuwen.studio.agent.agentbase.model.SystemConfig;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KnowledgeBaseSystemConfigService {

    private final static String SYSTEM_DOMAIN_ID = "SYSTEM";

    private final KnowledgeBaseSystemConfigMapper systemConfigMapper;

    private final Cache<String, Long> systemConfigCache;

    public KnowledgeBaseSystemConfigService(KnowledgeBaseSystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
        this.systemConfigCache = Caffeine.newBuilder()
            .initialCapacity(5)
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    public Long getSystemConfigValue(String id, String domainId) {
        String key = id + "|" + domainId;
        return systemConfigCache.get(key, k -> querySystemConfigValue(id, domainId));
    }

    private Long querySystemConfigValue(String id, String domainId) {
        SystemConfig systemConfig = systemConfigMapper.find(id, domainId);
        if (systemConfig == null) {
            systemConfig = systemConfigMapper.find(id, SYSTEM_DOMAIN_ID);
        }
        return Long.parseLong(systemConfig.getValue());
    }

    public SystemConfig querySystemConfigByDomainId(String id, String domainId) {
        return systemConfigMapper.find(id, domainId);
    }

}
