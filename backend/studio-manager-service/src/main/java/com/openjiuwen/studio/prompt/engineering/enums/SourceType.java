/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.enums;

public enum SourceType {
    /**
     * 0 标识是历史数据
     */
    HISTORY(0),
    /**
     * 1 标识是候选
     */
    CANDIDATE(1)
    ;
    private Integer type;

    SourceType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
