/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.annotation.Configuration;

/**
 * 功能描述 OBS配置信息
 *
 */
@Configuration
@Getter
@Setter
public class ObsConfig {
    private String bucketName;

    private String ak;

    private String sk;

    private String endPoint;

    private String type;

    private int retention;

    private int timeRefresh;

    private int maxConnections;

    private int maxPoolSize;

    private int corePoolSize;

    private int capacity;

    private String uploadFilePath;
}
