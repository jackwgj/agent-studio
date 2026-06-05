/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.ability;

import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KnowledgeListProcessor implements AbilityProcessor {

    @Override
    public AbilityTypeEnum ability() {
        return AbilityTypeEnum.KNOWLEDGE_LIST;
    }

    @Override
    public String processSubAbility(String userSubAbility, String metaSubAbility) {
        log.info("Knowledge list does not support subability");
        return null;
    }
}
