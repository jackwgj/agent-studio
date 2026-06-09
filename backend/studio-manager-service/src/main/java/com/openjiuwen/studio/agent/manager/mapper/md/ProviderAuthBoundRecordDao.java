/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.md;

import com.google.common.collect.ImmutableMap;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthBoundRecordPo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * ProviderAuthBoundRecordDao
 *
 */
@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProviderAuthBoundRecordDao {
    private final ProviderAuthBoundRecordMapper
        providerAuthBoundRecordMapper;

    private static final String DOMAIN_ID_FILTER_KEY = "DOMAIN_ID";

    private static final String PROVIDER_ID_FILTER_KEY = "PROVIDER_ID";

    /**
     * checkExistAuthBoundRecord
     *
     * @param domainId domainId
     * @param providerId provider
     * @return ProviderAuthBoundRecordPo
     */
    public boolean checkExistAuthBoundRecord(String domainId, String providerId) {
        if (StringUtils.isAnyBlank(domainId, providerId)) {
            log.error("Cannot query provider auth bound record in db: blank domainId or providerId");
            throw new AgentStudioException(StudioError.MD_PROVIDER_AUTH_BOUND_QUERY_FAILED_BAD_PARAM);
        }

        Map<String, Object> findCondition = ImmutableMap.of(DOMAIN_ID_FILTER_KEY, domainId, PROVIDER_ID_FILTER_KEY,
            providerId);
        List<ProviderAuthBoundRecordPo> providerAuthBoundRecordPos = providerAuthBoundRecordMapper.findByCons(
            findCondition);

        if (CollectionUtils.isEmpty(providerAuthBoundRecordPos)) {
            log.info("Provider auth bound record of domain {} and provider {} is nonexistent.", domainId, providerId);
            return false;
        }

        if (providerAuthBoundRecordPos.size() > 1) {
            log.error("Got more than one provider auth bound record of domain {} and provider {} in DB.", domainId,
                providerId);
            throw new AgentStudioException(StudioError.MD_PROVIDER_AUTH_BOUND_QUERY_FAILED_MORE_THAN_ONE_RECORD);
        }

        return true;
    }

    /**
     * saveUserBoundAuthRecord
     *
     * @param providerAuthBoundRecordPo providerAuthBoundRecordPo
     * @return int
     */
    public int saveUserBoundAuthRecord(ProviderAuthBoundRecordPo providerAuthBoundRecordPo) {
        return providerAuthBoundRecordMapper.save(providerAuthBoundRecordPo);
    }
}
