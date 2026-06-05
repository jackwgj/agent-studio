/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums.controller;

import lombok.Getter;

public enum ActionAfterCompletion {
    WAIT_USER_INPUT("WaitUserInput"),
    CONTINUE("Continue"),
    TERMINAL("Terminal");

    @Getter
    private final String action;

    ActionAfterCompletion(String action) {
        this.action = action;
    }
}
