/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL) // 全局忽略空字段
public class AuthRequestWrapper {
    @NotNull
    private Auth auth;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Auth {
        @NotNull
        private Identity identity;

        @NotNull
        private Scope scope;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Identity {
        @NotNull
        private List<String> methods;

        // 动态认证字段（互斥）
        private PasswordCredential password;

        private TokenCredential token;

        private AssumeRoleCredential assume_role;

        private AkSkCredential hw_ak_sk;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Scope {
        @NotNull
        private Project project;

        @NotNull
        private Domain domain;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Project {
        private String name;

        private String id;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Domain {
        private String name;

        private String id;
    }
}
