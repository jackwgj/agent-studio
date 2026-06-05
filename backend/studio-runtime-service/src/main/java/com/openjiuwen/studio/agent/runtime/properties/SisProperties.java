/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SisProperties
 *
 * @Date: 2025/8/19 15:00
 * @Description:
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "agent.builder.sis")
public class SisProperties {
    /**
     * 租户名
     */
    private String account;

    /**
     * 用户名称
     */
    private String user;

    /**
     * 用户密码密文
     */
    private String pwd;

    /**
     * Access Key
     */
    private String ak;

    /**
     * Secret Key
     */
    private String sk;

    /**
     * domain name
     */
    private String domainName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目ID
     */
    private String projectId;
}
