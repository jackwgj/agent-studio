/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private Token token;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Token {
        @JsonProperty("expires_at")
        private String expiresAt;

        private List<String> methods;

        private List<ServiceCatalog> catalog;

        private List<Role> roles;

        private Project project;

        @JsonProperty("issued_at")
        private String issuedAt;

        private User user;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceCatalog {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        private List<Endpoint> endpoints;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Endpoint {
        @JsonProperty("id")
        private String id;

        @JsonProperty("interface")
        private String endpointInterface;

        @JsonProperty("region")
        private String region;

        @JsonProperty("region_id")
        private String regionId;

        @JsonProperty("url")
        private String url;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Role {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Project {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        private Domain domain;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        private Domain domain;

        @JsonProperty("password_expires_at")
        private String passwordExpiresAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Domain {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }
}
