/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.ability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;
import com.openjiuwen.studio.agent.foundation.connection.model.ability.RetrieveSubAbility;
import com.openjiuwen.studio.agent.foundation.connection.model.ability.SearchMode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 处理RETRIEVE子能力
 */
@Slf4j
@Component
public class RetrieveProcessor implements AbilityProcessor {

    private final ObjectMapper mapper;

    public RetrieveProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AbilityTypeEnum ability() {
        return AbilityTypeEnum.RETRIEVE;
    }

    /**
     * 处理检索的子能力（目前子能力有：检索模式）
     *
     * @param usedSubAbility
     * @param metaSubAbility
     * @return
     */
    @Override
    public String processSubAbility(String usedSubAbility, String metaSubAbility) {
        try {
            RetrieveSubAbility usedRetrieveSubAbility = mapper.readValue(usedSubAbility, RetrieveSubAbility.class);
            RetrieveSubAbility metaRetrieveSubAbility = mapper.readValue(metaSubAbility, RetrieveSubAbility.class);

            List<SearchMode> usedSearchModes = Optional.ofNullable(usedRetrieveSubAbility)
                .map(RetrieveSubAbility::getSearchModeList)
                .orElse(new ArrayList<>());
            List<SearchMode> metaSearchModes = Optional.ofNullable(metaRetrieveSubAbility)
                .map(RetrieveSubAbility::getSearchModeList)
                .orElse(new ArrayList<>());

            if (CollectionUtils.isEmpty(metaSearchModes)) {
                return "";
            }

            // 1. 获取用户配置的搜索模式
            Set<String> usedEnabledModes = new HashSet<>();
            if (!CollectionUtils.isEmpty(usedSearchModes)) {
                usedSearchModes.forEach(node -> {
                    if (node.getSearchMode() != null) {
                        usedEnabledModes.add(node.getSearchMode());
                    }
                });
            }

            // 2. 遍历 meta 中的搜索模式，并根据用户配置的搜索模式，进行补全
            List<SearchMode> finalSearchModes = metaSearchModes.stream()
                .filter(m -> usedEnabledModes.contains(m.getSearchMode()))
                .toList();

            RetrieveSubAbility result = new RetrieveSubAbility();
            result.setSearchModeList(finalSearchModes);
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Fail to process RETRIEVE subability", e);
            throw new AgentBaseException("Fail to process RETRIEVE subability");
        }
    }
}
