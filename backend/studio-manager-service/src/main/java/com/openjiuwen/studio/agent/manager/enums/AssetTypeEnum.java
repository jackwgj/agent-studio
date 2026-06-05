/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * card资产类型枚举
 *
 */
@AllArgsConstructor
@Getter
public enum AssetTypeEnum {
    PLATFORM("platform", "平台预置"),
    WIDGETS("widgets", "自定义");

    @Getter
    private final String code;

    @Getter
    private final String desc;

}
