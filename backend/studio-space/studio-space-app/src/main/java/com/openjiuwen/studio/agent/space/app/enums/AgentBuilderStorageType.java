/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.enums;

import lombok.Getter;

/**
 * 文件保存地址类型
 */
public enum AgentBuilderStorageType {
    DB(0),
    OBS(1),
    ;

    @Getter
    private final int type;

    AgentBuilderStorageType(int type) {
        this.type = type;
    }
}