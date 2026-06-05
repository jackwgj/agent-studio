/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * JiuWen auth user配置类型
 *
 */
@Data
public class AuthUserConfig {
    @Data
    public static class Info {
        private String domain;

        @JsonProperty("auth_keys")
        private List<String> authKeys;
    }

    private String scope;

    private Info source;

    private Info target;
}
