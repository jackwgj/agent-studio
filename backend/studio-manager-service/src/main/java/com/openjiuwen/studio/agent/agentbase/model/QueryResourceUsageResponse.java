/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.model;

import lombok.Getter;

import java.io.Serializable;

/**
 * 查询资源使用返回体
 */
public class QueryResourceUsageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private Long maxValue = null;

    public QueryResourceUsageResponse setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    @Getter
    private Long usedValue = null;

    public QueryResourceUsageResponse setUsedValue(Long usedValue) {
        this.usedValue = usedValue;
        return this;
    }
}