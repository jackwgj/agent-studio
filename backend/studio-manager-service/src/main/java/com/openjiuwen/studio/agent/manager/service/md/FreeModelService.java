/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FreeModelService {
    @Value("${model.free-model.cfg:}")
    private String modelCfgs;

    @Getter
    private final Map<String, String> attrToModelId = new HashMap<>(2);

    private final Map<String, String> modelToAttrCode = new HashMap<>(2);

    @PostConstruct
    public void postConstruct() {
        log.info("model.free-model.cfg:{}", modelCfgs);
        if (StringUtils.isEmpty(modelCfgs)) {
            log.warn("Free model is empty.");
            return;
        }
        String[] vs = modelCfgs.split(",");
        for (String value : vs) {
            String[] kv = value.split("#");
            if (kv.length < 2) {
                log.error("model.free-model.cfg is invalid. kv:{}", value);
                continue;
            }
            attrToModelId.put(kv[1], kv[0]);
            modelToAttrCode.put(kv[0], kv[1]);
        }
    }

    public boolean isFreeModel(String modelId) {
        return modelToAttrCode.containsKey(modelId);
    }
}
