/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.properties;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import jakarta.annotation.PostConstruct;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IamProperties
 *
 * @Date: 2024/6/18 14:13
 * @Description: iam配置项
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "agent.builder.iam")
public class IamProperties {
    /**
     * IAM访问地址
     */
    private String domain;

    /**
     * IAM租户名
     */
    private String account;

    /**
     * IAM用户名称
     */
    private String user;

    /**
     * IAM用户密码密文
     */
    private String password;

    /**
     * IAM项目名称
     */
    private String projectName;

    /**
     * 项目id
     */
    private String projectId;

    @PostConstruct
    public void decryptSensitiveFields() {
        this.password = CryptoUtils.decrypt(this.password);
    }
}

