/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordCredential {
    private User user;

    @Data
    @Builder
    public static class User {
        private String name;

        private String password;

        private Domain domain;
    }

    @Data
    @Builder
    public static class Domain {
        private String name;
    }
}
