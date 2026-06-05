/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.enums;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

/**
 * 九问参数提取事件标识枚举
 *
 */
public enum ParamExtractionType {
    START,
    LLM,
    EXTENSION_BEFORE_ENTRY,
    CYCLE_BEGIN,
    DOMAIN_OBJECTS,
    EXTENSION_BEFORE_JUDGE_QUIT,
    EXTENSION_AFTER_EXTRACTION,
    END_EXCEPTION,
    END;

    private static final List<String> PARAM_EXTRACT_TYPES;

    static {
        ImmutableList.Builder<String> paramExtractTypesBuilder = new ImmutableList.Builder<>();
        for (ParamExtractionType paramExtractionType : ParamExtractionType.values()) {
            paramExtractTypesBuilder.add(paramExtractionType.name().toLowerCase(Locale.ROOT));
        }

        PARAM_EXTRACT_TYPES = paramExtractTypesBuilder.build();
    }

    public static List<String> obtainParamExtractTypes() {
        return PARAM_EXTRACT_TYPES;
    }
}
