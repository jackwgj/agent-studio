/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums.controller;

import lombok.Getter;

import java.util.Arrays;

public enum IntentHandlerType {
    WORKFLOW("Workflow", "Workflow"),
    TEXT("Text", "Text");

    @Getter
    private final String dsl;

    @Getter
    private final String ir;

    IntentHandlerType(String dsl, String ir) {
        this.dsl = dsl;
        this.ir = ir;
    }

    public static String dslToIr(String dsl) {
        return Arrays.stream(IntentHandlerType.values())
            .filter(type -> type.dsl.equals(dsl))
            .findFirst()
            .map(IntentHandlerType::getIr)
            .orElseThrow(() -> new IllegalArgumentException(String.format("Illegal dsl: %s", dsl)));
    }
}
