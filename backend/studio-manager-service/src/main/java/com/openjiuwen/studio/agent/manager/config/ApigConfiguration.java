/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "apig.op")
public class ApigConfiguration {
    /**
     * apig访问域名
     */
    private String domain;

    /**
     * apig实例id
     */
    private String instanceId;

    /**
     * roma app id
     */
    private String romaAppId;

    /**
     * credentials id
     */
    private String credentialsId;

    /**
     * env id
     */
    private String envId;

    private String httpType;

    private String host;
}
