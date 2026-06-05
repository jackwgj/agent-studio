/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.environment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * ops服务查询实例接口响应数据
 *
 */
@Data
public class OpsEnvironmentInstances {
    @JsonProperty("items")
    private List<OpsEnvironmentInstance> items;

    @JsonProperty("total")
    private int total;
}
