/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.ability;

import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 处理配置第三方模型的子能力
 */
@Slf4j
@Component
public class ThirdPartyModelProcessor implements AbilityProcessor {

    @Override
    public AbilityTypeEnum ability() {
        return AbilityTypeEnum.THIRD_PARTY_MODEL;
    }

    @Override
    public String processSubAbility(String userSubAbility, String metaSubAbility) {
        log.info("Third-party model does not support subability");
        return null;
    }
}
