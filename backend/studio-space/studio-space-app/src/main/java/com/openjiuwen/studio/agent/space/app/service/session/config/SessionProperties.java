/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * session配置
 */
@Data
@Component
public class SessionProperties {
    @Value("${agent.space.session.timeout:30}")
    private long timeOut;

    @Value("${agent.space.session.redistimeout:30}")
    private long redisTimeOut;

    @Value("${agent.space.session.maxtimeout:1440}")
    private long maxTimeOut;
}
