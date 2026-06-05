/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.config;

import com.openjiuwen.studio.agent.space.common.utils.SpringContextUtils;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Redis链接配置信息
 */
@Data
@ConfigurationProperties(prefix = "agent.apace.apigw.signature")
@DependsOn(value = "springContextUtils")
@Component
public class ApiGWSignatureConfigInfo {
    private String key;

    private String secret;

    public static ApiGWSignatureConfigInfo getInstance() {
        return SpringContextUtils.getBean(ApiGWSignatureConfigInfo.class);
    }
}
