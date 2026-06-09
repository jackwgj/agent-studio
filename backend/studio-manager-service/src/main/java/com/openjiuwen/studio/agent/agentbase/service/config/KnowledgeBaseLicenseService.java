/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.agentbase.enums.LicenseAttrCode;
import com.openjiuwen.studio.agent.agentbase.model.SystemConfig;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.common.service.contrlller.ResourceInstInternApi;
import com.openjiuwen.studio.common.service.model.skuinst.LicenseInst;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KnowledgeBaseLicenseService {

    private final static long BASE_UNIT = 1024L * 1024 * 1024;

    private final KnowledgeBaseSystemConfigService configService;

    private final Cache<String, Long> licenseCache;

    private final ResourceInstInternApi resourceInstInternApi;

    @Value("${license.enable:true}")
    private boolean licenseEnable;

    @Value("${license.whitelist.enable:false}")
    private boolean whiteListEnable;

    public KnowledgeBaseLicenseService(KnowledgeBaseSystemConfigService configService,
        ResourceInstInternApi resourceInstInternApi) {
        this.configService = configService;
        this.resourceInstInternApi = resourceInstInternApi;
        this.licenseCache = Caffeine.newBuilder()
            .initialCapacity(5)
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    public long getLimitValue(String id, LicenseAttrCode attrCode) {
        if (licenseEnable) {
            // 白名单，可在数据库中插入用户数据，优先从数据库中获取。默认情况关闭
            if (whiteListEnable) {
                SystemConfig systemConfig = configService.querySystemConfigByDomainId(id,
                    RequestContextUtils.getRequestUserDomainId());
                if (systemConfig != null) {
                    return Long.parseLong(systemConfig.getValue());
                }
            }
            return getLicenseItemValue(id, attrCode);
        }
        return configService.getSystemConfigValue(id, RequestContextUtils.getRequestUserDomainId());
    }

    public long getLicenseItemValue(String domainId, LicenseAttrCode attrCode) {
        String key = domainId + "|" + attrCode;
        return licenseCache.get(key, k -> getItemValue(attrCode));
    }

    private long getItemValue(LicenseAttrCode attrCode) {
        try {
            ResponseEntity<List<LicenseInst>> listResponseEntity = resourceInstInternApi.queryLicense(null, null, null,
                null, RequestContextUtils.getRequestUserDomainId());
            List<LicenseInst> licenseList = listResponseEntity.getBody();
            return licenseList.stream()
                .filter(licenseInst -> licenseInst.getAttrCode().equals(attrCode.getValue()))
                .mapToLong(licenseInst -> {
                    try {
                        return Long.parseLong(licenseInst.getMaxValue());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum() * BASE_UNIT;
        } catch (RuntimeException ex) {
            log.error("Get license item failed {}", ex);
            throw new AgentBaseException(ErrorCode.QUERY_KNOWLEDGE_BASE_LICENSE_ITEM_ERROR, ex);
        }

    }

}
