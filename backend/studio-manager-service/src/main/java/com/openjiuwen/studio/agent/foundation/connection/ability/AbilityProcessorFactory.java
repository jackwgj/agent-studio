/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.ability;

import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AbilityProcessorFactory {

    private final Map<AbilityTypeEnum, AbilityProcessor> processorMap = new ConcurrentHashMap<>();

    @Autowired
    public AbilityProcessorFactory(List<AbilityProcessor> processors) {
        processors.forEach(p -> processorMap.put(p.ability(), p));
    }

    public AbilityProcessor getProcessor(String ability) {
        AbilityTypeEnum abilityType = AbilityTypeEnum.valueOf(ability.toUpperCase(Locale.ENGLISH));

        if (!AbilityTypeEnum.isValid(abilityType)) {
            log.error("Does not support ability {}", ability);
            throw new IllegalArgumentException();
        }

        AbilityProcessor processor = processorMap.get(abilityType);

        if (processor == null) {
            log.error("Processor for ability {} not implement", ability);
            throw new NotImplementedException();
        }
        return processor;
    }
}
