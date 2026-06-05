/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.service;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CopyUtils;
import com.openjiuwen.studio.agent.common.utils.ValidateUtils;
import com.openjiuwen.studio.common.service.entity.TenantEntity;
import com.openjiuwen.studio.common.service.model.tenant.Tenant;
import com.openjiuwen.studio.common.service.repository.TenantRepository;

import io.netty.util.internal.StringUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class TenantServiceUtil {

    private static TenantRepository tenantRepository;
    private static RedisClient redisClient;

    private static Boolean redisEnable;

    @Value("${redis.enabled:true}")
    private Boolean privateRedisEnable;

    @Autowired
    private TenantRepository privateTenantRepository;

    @Autowired
    private RedisClient privateRedisClient;

    @PostConstruct
    private void setStaticRedisEnableValue() {
        redisEnable = this.privateRedisEnable;
        tenantRepository = this.privateTenantRepository;
        redisClient = this.privateRedisClient;

    }

    public static Tenant queryByDomainId(String domainId) {
        try {
            log.info("AgentBuilder query tenant info start");
            if (ValidateUtils.isEmptyString(domainId)) {
                throw new AgentStudioException(StudioError.PARAMS_IS_REQUIRED);
            }
            TenantEntity tenantInfo = tenantRepository.selectById(domainId);
            if (tenantInfo != null) {
                return (Tenant) CopyUtils.copyProperties(tenantInfo, new Tenant());
            }
        } catch (Exception e) {
            log.error("AgentBuilder query tenant info failed", e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        return null;
    }

    public static Tenant getTenantInfo(String domainId) {
        if (StringUtil.isNullOrEmpty(domainId)) {
            return null;
        } else {
            Tenant tenant = null;
            if (redisEnable) {
                tenant = getTenantInfoFromCache(domainId);
            }

            if (tenant == null) {
                tenant = queryByDomainId(domainId);
                setTenantInfoToCache(tenant);
            }

            return tenant;
        }
    }

    private static void setTenantInfoToCache(Tenant tenant) {
        if (tenant != null && !ValidateUtils.isEmptyString(tenant.getDomainId())) {
            if (redisEnable) {
                String userKey = processUserKey(tenant.getDomainId());
                redisClient.set(userKey, JSONObject.toJSONString(tenant), Duration.ofSeconds(600L));
            }

        }
    }

    private static Tenant getTenantInfoFromCache(String domainId) {
        if (ValidateUtils.isEmptyString(domainId)) {
            return null;
        } else {
            String tenantKey = processUserKey(domainId);
            if (redisEnable) {
                String tenantInfoStr = redisClient.get(tenantKey);
                return ValidateUtils.isEmptyString(tenantInfoStr)
                    ? null
                    : JSONObject.parseObject(tenantInfoStr, Tenant.class);
            } else {
                return null;
            }
        }
    }

    private static String processUserKey(String domainId) {
        return "AGENT_BUILDER__TENANT_" + domainId;
    }
}
