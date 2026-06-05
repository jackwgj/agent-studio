/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.enums;

import java.util.Map;

public enum DifyAuthType {
    BEARER("bearer") {
        @Override
        public void fillKeyPair(Map<String, String> keyPair, Map<String, Object> config) {
            keyPair.put("target_name", "Authorization");
            keyPair.put("auth_key", "Bearer " + config.getOrDefault("api_key", ""));
        }
    },
    CUSTOM("custom") {
        @Override
        public void fillKeyPair(Map<String, String> keyPair, Map<String, Object> config) {
            String header = String.valueOf(config.getOrDefault("header", "X-Api-Key"));
            keyPair.put("target_name", header);
            keyPair.put("auth_key", String.valueOf(config.getOrDefault("api_key", "")));
        }
    },
    BASIC("basic") {
        @Override
        public void fillKeyPair(Map<String, String> keyPair, Map<String, Object> config) {
            keyPair.put("target_name", "Authorization");
            keyPair.put("auth_key", String.valueOf(config.getOrDefault("api_key", "")));
        }
    },
    NO_AUTH("no-auth") {
        @Override
        public void fillKeyPair(Map<String, String> keyPair, Map<String, Object> config) {}
    },
    API_KEY("api-key") {
        @Override
        public void fillKeyPair(Map<String, String> keyPair, Map<String, Object> config) {};
    };

    private final String value;

    DifyAuthType(String value) {
        this.value = value;
    }

    public abstract void fillKeyPair(Map<String, String> keyPair, Map<String, Object> config);

    public static DifyAuthType fromValue(String type) {
        for (DifyAuthType t : values()) {
            if (t.value.equalsIgnoreCase(type)) {
                return t;
            }
        }
        return NO_AUTH;
    }
}
