/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 数据源健康检查请求体
 *
 */
@Data
@Builder
public class AskDatasourceReq {
    private String query;

    @JsonProperty("should_by_cache")
    private boolean shouldByCache;

    private List<String> conditions;
}
