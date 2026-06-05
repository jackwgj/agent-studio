/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 工具鉴权秘钥信息
 */
@ApiModel(description = "工具鉴权秘钥信息")

@Validated

public class AuthKeyInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("target_name")
    @NotBlank
    @Length(min = 1, max = 64)
    private String targetName = null;

    @JsonProperty("source_name")
    @Length(max = 64)
    private String sourceName = null;

    @JsonProperty("auth_key")
    @Length(max = 20000)
    private String authKey = null;

    @JsonProperty("desc")
    @Length(max = 128)
    private String desc = null;

    public String getTargetName() {
        return targetName;
    }

    public AuthKeyInfo setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public AuthKeyInfo setSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public String getAuthKey() {
        return authKey;
    }

    public AuthKeyInfo setAuthKey(String authKey) {
        this.authKey = authKey;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public AuthKeyInfo setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthKeyInfo {\n");

        sb.append("    targetName: ").append(toIndentedString(targetName)).append("\n");
        sb.append("    sourceName: ").append(toIndentedString(sourceName)).append("\n");
        sb.append("    authKey: ").append(toIndentedString(authKey)).append("\n");
        sb.append("    desc: ").append(toIndentedString(desc)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthKeyInfo authKeyInfo = (AuthKeyInfo) o;
        return Objects.equals(this.targetName, authKeyInfo.targetName) && Objects.equals(this.sourceName,
            authKeyInfo.sourceName) && Objects.equals(this.authKey, authKeyInfo.authKey) && Objects.equals(this.desc,
            authKeyInfo.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetName, sourceName, authKey, desc);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
