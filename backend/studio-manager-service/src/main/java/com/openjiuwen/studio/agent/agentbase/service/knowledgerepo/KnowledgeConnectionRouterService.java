/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import static com.openjiuwen.studio.agent.agentbase.common.constant.KnowledgeLockConstant.KNOWLEDGE_USAGE_CALCULATE_LOCK;
import static com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode.FAILED_TO_FIND_DEFAULT_KNOWLEDGE_SOURCE;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.DEFAULT_DOMAIN_ID;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.DEFAULT_TENANT_TYPE;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.NO_TENANT_INFO;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2B;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2C;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.agentbase.entity.KbConnectionRouterEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KbConnectionRouterMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseStatistics;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.routerstrategy.KbRouterContext;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 资源映射服务
 * 1. 根据tenantType找到对应的知识库connectionId
 */
@Slf4j
@Service
public class KnowledgeConnectionRouterService {

    public static final String NO_CONNECTOR = "no_connector";

    private final Cache<String, List<KbConnectionRouterEntity>> tenantTypeConnectionRouterCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    private final KbConnectionRouterMapper kbConnectionRouterMapper;

    private final RedisClient redisClient;

    private final KbRouterContext kbRouterContext;

    @Value("${env.type}")
    private String deployMode;

    public KnowledgeConnectionRouterService(KbConnectionRouterMapper kbConnectionRouterMapper, RedisClient redisClient,
        KbRouterContext kbRouterContext) {
        this.kbConnectionRouterMapper = kbConnectionRouterMapper;
        this.redisClient = redisClient;
        this.kbRouterContext = kbRouterContext;
    }

    public @NotNull String findConnectionIdByContext() {
        if (com.openjiuwen.studio.agent.foundation.base.constants.DeploymentModeEnum.isNotHc(deployMode)) {
            // 非HC场景，直接获取
            return getConnectionIdByContext(DEFAULT_TENANT_TYPE, DEFAULT_DOMAIN_ID);
        }
        // 从SDK中获取用户的tenant_type，若无此type，则选取默认connectionId
        String tenantType = TenantTypeUtil.getTenantType();
        String domainId = RequestContextUtils.getRequestUserDomainId();

        if (NO_TENANT_INFO.equals(tenantType)) {
            return NO_CONNECTOR;
        }

        if (!TENANT_TYPE_2B.equals(tenantType)) {
            // 如果不是收费场景，统一使用default_domain_id
            domainId = DEFAULT_DOMAIN_ID;
        }
        log.info("user tenant type is {}.", tenantType);
        try {
            return getConnectionIdByContext(tenantType, domainId);
        } catch (RuntimeException exception) {
            log.error("failed to find connectionId from context!", exception.getMessage());
            throw new AgentBaseException(FAILED_TO_FIND_DEFAULT_KNOWLEDGE_SOURCE);
        }
    }

    private String getConnectionIdByContext(String tenantType, String domainId) {
        List<KbConnectionRouterEntity> kbConnectionRouterEntities = getKbConnectionEntities(tenantType, domainId);
        return kbRouterContext.getRouterStrategyFromContext().findConnectionId(kbConnectionRouterEntities);
    }

    public @NotNull String findConnectionIdByKerberosAuthMode() {
        return getConnectionIdByContext(DEFAULT_TENANT_TYPE, DEFAULT_DOMAIN_ID);
    }

    /**
     * 定时计算每个知识库连接下已经创建的知识库个数,每20分钟计算一次
     */
    //@Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES)
    void calculateKbUsedNumberUnderEachConnection() {
        RedisLock lock = redisClient.getLock(KNOWLEDGE_USAGE_CALCULATE_LOCK);
        try {
            // 尝试加锁，最多等待 0 秒，锁自动释放时间为 5分钟（防止死锁）
            if (lock.tryLock(Duration.ofMinutes(10))) {
                log.info("succeed to acquire redis lock: {}", KNOWLEDGE_USAGE_CALCULATE_LOCK);
                List<KnowledgeBaseStatistics> knowledgeBaseUsedStatistics
                    = kbConnectionRouterMapper.calculateRepoNumUnderEachConnection();
                knowledgeBaseUsedStatistics.forEach(v -> log.info(v.toString()));
                kbConnectionRouterMapper.updateKnowledgebaseUsedNum(knowledgeBaseUsedStatistics);
            } else {
                log.warn("failed to acquire redis lock: {}", KNOWLEDGE_USAGE_CALCULATE_LOCK);
            }
        } catch (RuntimeException exception) {
            log.error("fail to update used knowledge repo number reason is {}", exception);
            throw new AgentBaseException("fail to update used knowledge repo number");
        } catch (Exception e) {
            log.error("fail to update used knowledge repo number reason is {}", e);
            throw new AgentBaseException("fail to update used knowledge repo number because of Exception");
        }
    }

    public List<String> findFreeConnectionIds() {
        List<KbConnectionRouterEntity> kbConnectionRouterEntities = getKbConnectionEntities(TENANT_TYPE_2C,
            DEFAULT_DOMAIN_ID);
        return kbConnectionRouterEntities.stream().map(KbConnectionRouterEntity::getKnowledgeBaseConnectionId).toList();
    }

    private @NotNull List<KbConnectionRouterEntity> getKbConnectionEntities(String tenantType, String domainId) {
        List<KbConnectionRouterEntity> kbConnectionRouterEntities = tenantTypeConnectionRouterCache.getIfPresent(
            tenantType + "|" + domainId);
        if (CollectionUtils.isEmpty(kbConnectionRouterEntities)) {
            kbConnectionRouterEntities = kbConnectionRouterMapper.selectByTenantTypeAndDomainId(tenantType, domainId);
            kbConnectionRouterEntities.forEach(v -> {
                v.setUsageRatio(v.getKnowledgeBaseCapacity() == 0
                    ? Float.MAX_VALUE
                    : (float) v.getKnowledgeBaseUsed() / v.getKnowledgeBaseCapacity());
            });
            tenantTypeConnectionRouterCache.put(tenantType + "|" + domainId, kbConnectionRouterEntities);
        }
        return kbConnectionRouterEntities;
    }
}
