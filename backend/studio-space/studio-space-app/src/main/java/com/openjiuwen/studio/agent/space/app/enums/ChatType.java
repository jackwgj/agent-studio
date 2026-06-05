/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 0：首次用户问答；1：后续用户补充计划；-1：暂停；-2：手动终止
 */
@Slf4j
public enum ChatType {
    // 首次用户问答
    FIRST_USER_QA(0),
    // 后续用户补充计划
    REPLENISHMENT_PROGRAM(1),
    // 暂停
    PAUSE(-1),
    // 手动终止
    STOP(-2);

    @Getter
    private final int type;

    ChatType(int type) {
        this.type = type;
    }
}
