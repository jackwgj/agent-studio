/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 工具鉴权信息
 */
@ApiModel(description = "工具鉴权信息")

@Validated

public class AuthInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("scope")
    private ScopeEnum scope = null;

    @JsonProperty("domain")
    private DomainEnum domain = null;

    @JsonProperty("auth_keys")
    @Valid
    @Size(min = 1, max = 5)
    private List<AuthKeyInfo> authKeys = null;

    public ScopeEnum getScope() {
        return scope;
    }

    public AuthInfo setScope(ScopeEnum scope) {
        this.scope = scope;
        return this;
    }

    public DomainEnum getDomain() {
        return domain;
    }

    public AuthInfo setDomain(DomainEnum domain) {
        this.domain = domain;
        return this;
    }

    public List<AuthKeyInfo> getAuthKeys() {
        return authKeys;
    }

    public AuthInfo setAuthKeys(List<AuthKeyInfo> authKeys) {
        this.authKeys = authKeys;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthInfo {\n");

        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
        sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
        sb.append("    authKeys: ").append(toIndentedString(authKeys)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthInfo authInfo = (AuthInfo) o;
        return Objects.equals(this.scope, authInfo.scope) && Objects.equals(this.domain, authInfo.domain)
            && Objects.equals(this.authKeys, authInfo.authKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, domain, authKeys);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * 工具鉴权方式，分为用户级、服务级和IAM方式
     */
    public enum ScopeEnum {
        USER("USER"),

        SERVICE("SERVICE");

        private String value;

        ScopeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ScopeEnum fromValue(String text) {
            for (ScopeEnum b : ScopeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * 工具鉴权秘钥位置
     */
    public enum DomainEnum {
        HEADERS("HEADERS"),

        QUERY("QUERY");

        private String value;

        DomainEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static DomainEnum fromValue(String text) {
            for (DomainEnum b : DomainEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
