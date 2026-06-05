/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Data
@Component
@ConfigurationProperties(prefix = "user.proxy")
public class UserProxyConfig {
    private boolean enabled = false;

    private Set<String> noProxy = new HashSet<>() {{
        add("127.0.0.1");
    }};

    private String schema = "http";

    private String host;

    private Integer port = 8080;

    private String username;

    private String password;
}
