/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * IR适配器配置类，透传环境变量
 */
@Configuration
@Data
public class IRAdapterConfig {
    @Value("${datasource.query.max-length:5000}")
    private String datasourceQueryMaxLength;
}
