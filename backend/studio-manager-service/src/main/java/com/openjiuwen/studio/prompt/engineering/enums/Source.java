/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

/**
 * 模板来源枚举值
 */
public enum Source {
    /**
     * playground调优
     */
    PLAYGROUND,
    /**
     * 实验调优
     */
    EXPERIMENT,
    /**
     * 候选集candidate sets
     */
    CANDIDATE,
    /**
     * 横向评估
     */
    HORIZONTAL,

    /**
     * 无来源
     */
    NO_SOURCE,

    /**
     * 预置数据
     */
    PRESET
}
