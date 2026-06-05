/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.ability;

import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 处理OCR子能力
 */
@Slf4j
@Component
public class OcrProcessor implements AbilityProcessor {

    @Override
    public AbilityTypeEnum ability() {
        return AbilityTypeEnum.OCR;
    }

    @Override
    public String processSubAbility(String userSubAbility, String metaSubAbility) {
        log.info("OCR does not support subability");
        return null;
    }
}
